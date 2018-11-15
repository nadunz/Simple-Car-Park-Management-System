
import database.Connect;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nadun
 */
public class Dashboard extends javax.swing.JFrame {

    /**
     * Creates new form Dashboard
     */
    private Connect connect; //database connection
    
    public static final int LEVEL_1 = 0,LEVEL_2 = 1,LEVEL_3 = 2,DEFAULT = 3;
    public static final Color LEVEL_1_COLOR = new Color(204, 204, 0),
                              LEVEL_2_COLOR = new Color(255, 102, 255),
                              LEVEL_3_COLOR = new Color(255, 153, 51);
    
    private  DefaultTableModel all_customer_dtm,available_usage_dtm;
    private DefaultComboBoxModel<String> location_combo_model;
    
    private boolean isPrepaidCustomer = true;
    
    public Dashboard() {
        initComponents();
        this.setLocationRelativeTo(null);
        // initialize 2 tables and one location number combo box
        all_customer_dtm = (DefaultTableModel) all_customer_table.getModel();
        available_usage_dtm = (DefaultTableModel) available_usage_table.getModel();
        location_combo_model = (DefaultComboBoxModel<String>) location_num_combo_box.getModel();
        
        this.connect = new Connect();
        // set default level color to the label
        setLevelColor(DEFAULT);
        initInterfaces();
    }
    
    public void setLevelColor(int level){
        switch(level){
            case 0:
                level_display_label.setBackground(LEVEL_1_COLOR);
                level_display_label.setText("Season Park");
                break;
            case 1:
                level_display_label.setBackground(LEVEL_2_COLOR);
                level_display_label.setText("Top Management Park");
                break;
            case 2:
                level_display_label.setBackground(LEVEL_3_COLOR);
                level_display_label.setText("Visitor Park");
                break;
            default:
                level_display_label.setBackground(Color.WHITE);
                level_display_label.setText("PARK ?");
                break;
        }
    }
    
    public void initInterfaces(){
        parking_qr_txt.requestFocus();
        retrieveAllCustomers();
        retriveAllLocationsToCombo();
        updateDashBoard();
        retriveCurrentUsage();
    }
    
    private void addCustomerToTable(int index,String id,String name,String veh_num,String level,String renewDate,String status){ 
        all_customer_dtm.addRow(new Object[]{index,id,name,veh_num,level,renewDate,status});
    }
    
    private void addAvailableUsageToTable(String id,String veh_num,String level,int location){ 
        available_usage_dtm.addRow(new Object[]{id,veh_num,level,location});
    }
    
    public String getRenewDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Calendar originalDate = Calendar.getInstance();
        //System.out.println("The original Date is : " + originalDate.getTime());
        Calendar nextMonthDate = (Calendar) originalDate.clone();
        // Add the Date as next month ahead
        nextMonthDate.add(Calendar.MONTH, 1);
        
        return df.format(nextMonthDate.getTime());
    }
    
    public void clearRegForm(){
        level_combo_box.setSelectedIndex(0);
        qr_txt.setText("");
        id_txt.setText("");
        name_txt.setText("");
        vehicle_num_txt.setText("");
        qr_txt.requestFocus();
    }
    
    private void cleanAllCustomerTable(){
          all_customer_dtm.getDataVector().removeAllElements();
          all_customer_dtm.fireTableDataChanged();
    }
    
    private void clearCurrentUsageTable(){
          available_usage_dtm.getDataVector().removeAllElements();
          available_usage_dtm.fireTableDataChanged();
    }
    
    private void cleanLocationCombo(){
      location_combo_model.removeAllElements();
    }
    
    private void retriveAvailableLocationsToCombo(int level){
        cleanLocationCombo();
        List<Integer> usedLocations = new ArrayList<Integer>();
        // select parked vehicles
        String query="SELECT * FROM customer WHERE parking=1 AND level="+level;
             
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                int location=r.getInt("location_num");
                usedLocations.add(location);
            }
            // popup the combo box by available locations
            for (int loc_num = 1; loc_num <= 60; loc_num++) {
                boolean used = false;
                for (Integer usedLocation : usedLocations) {
                    if (usedLocation==loc_num) {
                        used = true;
                    }
                }
                if (!used) {
                    location_combo_model.addElement("No. "+loc_num);
                }
            }
            
        } catch (SQLException ex) {}
        
        
    }
    private void retriveAllLocationsToCombo(){
        cleanLocationCombo();
        
        // popup the combo box by all locations
        for (int loc_num = 1; loc_num <= 60; loc_num++) {
            
            location_combo_model.addElement("No. "+loc_num);
           
        }
      
    }
    
    private String getLevel(int level){
        String level_str = "";
        if(level==0) level_str = "Season Park";
        if(level==1) level_str = "Top Management Park";
        if(level==2) level_str = "Visitor Park";
        
        return level_str;
    }
    
    private boolean isExpired(String renewDate){
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        Date today = null;
        try {
            date = df.parse(renewDate);
            today = df.parse(df.format(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (today.compareTo(date) > 0);
    }
    
    public void retrieveAllCustomers(){
      cleanAllCustomerTable();
      String query="SELECT * FROM customer";
             
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                int index=r.getInt("serial_no");
                String id=r.getString("id_number");
                String name=r.getString("name");
                String veh_num=r.getString("veh_num");
                //convert int level value to string word
                String level = getLevel(r.getInt("level"));
                String renew_date = r.getString("renew_date");
                String status = null;
                if(!renew_date.equals("-"))
                    status = isExpired(renew_date)? "Expired":"Active";
                else
                    status = "-";
                    
                addCustomerToTable(index,id, name, veh_num, level,renew_date,status);
                
            }
        } catch (SQLException ex) {}
       
    }
    
    public void retriveCurrentUsage(){
      clearCurrentUsageTable();
      String query="SELECT * FROM customer WHERE parking=1";
             
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                
                String id=r.getString("id_number");
                String veh_num=r.getString("veh_num");
                //convert int level value to string word
                String level = getLevel(r.getInt("level"));
                int location_no = r.getInt("location_num");
               
                addAvailableUsageToTable(id, veh_num, level,location_no);
                
            }
        } catch (SQLException ex) {}
       
    }
    
    private void parkPrepaidCustomers(boolean parking,String qr,int location){
        int parking_int = 0;
        if (parking) parking_int = 1;
        else parking_int = 0;
 
        String query="UPDATE customer SET parking="+parking_int+",location_num="+location+" WHERE qr_code='"+qr+"'";
                
            boolean setQuery = connect.setQuery(query);
            if(setQuery){
////                String msg = "";
//                if(parking) msg = "The vehicle parked succesfully !";
//                else msg = "The vehicle removed from park succesfully !";
//                JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
                clearRegForm();
                retrieveAllCustomers();
            }
        
    }
    
     private void removeVisitor(String id){
        
        String query="DELETE FROM customer WHERE id_number='"+id+"'";
                
            boolean setQuery = connect.setQuery(query);
            if(setQuery){
                
                String msg = "The visitor's vehicle removed from park succesfully !";
                
                JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
                
                retrieveAllCustomers();
            }
    }
    
    private void reset(){
        // clear the text fields
        parking_qr_txt.setText("");
        parking_id_txt.setText("");
        parking_name_txt.setText("");
        parking_veh_num_txt.setText("");
        location_num_combo_box.setEnabled(true);
        park_unpark_btn.setText("Park");
        if (isPrepaidCustomer) {
            
            setLevelColor(DEFAULT);
            parking_qr_txt.setEnabled(true);
            parking_qr_lbl.setEnabled(true);
             //
            parking_id_txt.setEnabled(false);
            parking_veh_num_txt.setEnabled(false);
            parking_name_txt.setEnabled(false);
            
            retriveAllLocationsToCombo();
            
            parking_qr_txt.requestFocus();
            
        }else{
            
            setLevelColor(LEVEL_3);
            parking_qr_txt.setEnabled(false);
            parking_qr_lbl.setEnabled(false);
            //
            parking_id_txt.setEnabled(true);
            parking_veh_num_txt.setEnabled(true);
            parking_name_txt.setEnabled(true);
            
            retriveAvailableLocationsToCombo(LEVEL_3);
            
            parking_id_txt.requestFocus();
        }
    }
    
    private int getLocation(String selected){
        return Integer.parseInt(selected.substring(4));
    }
    
    private int getUsedSpacesOfPark(int level){
        int count = 0;
        String query = null;
        
        query="SELECT COUNT(serial_no) FROM customer WHERE parking=1 AND level="+level;
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                count=r.getInt("COUNT(serial_no)"); 
            }
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return count;
    }
    
    private void updateDashBoard(){
        level1_parking_count_lbl.setText("    "+getUsedSpacesOfPark(LEVEL_1)+"  Parked");
        level2_parking_count_lbl.setText("    "+getUsedSpacesOfPark(LEVEL_2)+"  Parked");
        level3_parking_count_lbl.setText("    "+getUsedSpacesOfPark(LEVEL_3)+"  Parked");
    }
    
    private boolean isIssued(String qr){
        int count = 0;
        String query = null;
        
        query="SELECT COUNT(serial_no) FROM customer WHERE qr_code='"+qr+"'";
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                count=r.getInt("COUNT(serial_no)"); 
            }
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(count==0) return false;
        else return true;
        
    }
    
    private boolean isRegistered(String id){
        int count = 0;
        String query = null;
        
        query="SELECT COUNT(serial_no) FROM customer WHERE id_number='"+id+"'";
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                count=r.getInt("COUNT(serial_no)"); 
            }
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(count==0) return false;
        else return true;
    }
    
    private boolean isParked(int index){
        int parking = 0;
        String query = null;
        
        query="SELECT parking FROM customer WHERE serial_no="+index+"";
        ResultSet r=connect.getQuery(query);
        try {
            while(r.next()){
                parking=r.getInt("parking"); 
            }
        } catch (SQLException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(parking==0) return false;
        else return true;
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        main_panel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        level1_parking_count_lbl = new javax.swing.JLabel();
        level2_parking_count_lbl = new javax.swing.JLabel();
        level3_parking_count_lbl = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        level_combo_box = new javax.swing.JComboBox<>();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        qr_txt = new javax.swing.JTextField();
        id_txt = new javax.swing.JTextField();
        name_txt = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        vehicle_num_txt = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        all_customer_table = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        register_btn = new javax.swing.JButton();
        clear_btn = new javax.swing.JButton();
        reactivate_btn = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        level_switch_toggle_btn = new javax.swing.JToggleButton();
        parking_qr_lbl = new javax.swing.JLabel();
        parking_qr_txt = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        parking_id_txt = new javax.swing.JTextField();
        reset_btn = new javax.swing.JButton();
        park_unpark_btn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        available_usage_table = new javax.swing.JTable();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        level_display_label = new javax.swing.JLabel();
        parking_name_txt = new javax.swing.JTextField();
        parking_veh_num_txt = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        location_num_combo_box = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Parking Management System ");
        setResizable(false);

        main_panel.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(153, 153, 255));
        jPanel2.setLayout(new java.awt.GridLayout(2, 3));

        level1_parking_count_lbl.setBackground(new java.awt.Color(204, 204, 0));
        level1_parking_count_lbl.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        level1_parking_count_lbl.setText("       0   Parked");
        level1_parking_count_lbl.setOpaque(true);
        jPanel2.add(level1_parking_count_lbl);

        level2_parking_count_lbl.setBackground(new java.awt.Color(255, 102, 255));
        level2_parking_count_lbl.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        level2_parking_count_lbl.setText("       0   Parked");
        level2_parking_count_lbl.setOpaque(true);
        jPanel2.add(level2_parking_count_lbl);

        level3_parking_count_lbl.setBackground(new java.awt.Color(255, 153, 51));
        level3_parking_count_lbl.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        level3_parking_count_lbl.setText("       0   Parked");
        level3_parking_count_lbl.setOpaque(true);
        jPanel2.add(level3_parking_count_lbl);

        jLabel5.setBackground(new java.awt.Color(204, 204, 0));
        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("/60");
        jLabel5.setOpaque(true);
        jPanel2.add(jLabel5);

        jLabel6.setBackground(new java.awt.Color(255, 102, 255));
        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("/60");
        jLabel6.setOpaque(true);
        jPanel2.add(jLabel6);

        jLabel7.setBackground(new java.awt.Color(255, 153, 51));
        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("/60");
        jLabel7.setOpaque(true);
        jPanel2.add(jLabel7);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rsz_car-parking-icon.png"))); // NOI18N
        jLabel11.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        jLabel1.setFont(new java.awt.Font("Tempus Sans ITC", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Parking Manager");

        jLabel8.setBackground(new java.awt.Color(102, 102, 102));
        jLabel8.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Season Park");

        jLabel9.setBackground(new java.awt.Color(102, 102, 102));
        jLabel9.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Top Management Park");

        jLabel10.setBackground(new java.awt.Color(102, 102, 102));
        jLabel10.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Visitor Park");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 463, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rsz_registration_image.png"))); // NOI18N

        level_combo_box.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        level_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Season pass", "Directors and top management" }));
        level_combo_box.setFocusable(false);

        jLabel22.setText("Level");

        jLabel23.setText("QR code");

        jLabel24.setText("ID");

        jLabel25.setText("Name");

        qr_txt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        qr_txt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                qr_txtKeyReleased(evt);
            }
        });

        id_txt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        name_txt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel26.setText("Vehicle No.");

        vehicle_num_txt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        all_customer_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Index", "ID", "Name", "Vehicle No.", "Level", "Renew Date", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        all_customer_table.setFocusable(false);
        all_customer_table.setGridColor(new java.awt.Color(204, 204, 204));
        all_customer_table.setRowHeight(20);
        all_customer_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(all_customer_table);
        if (all_customer_table.getColumnModel().getColumnCount() > 0) {
            all_customer_table.getColumnModel().getColumn(0).setPreferredWidth(25);
            all_customer_table.getColumnModel().getColumn(2).setPreferredWidth(150);
            all_customer_table.getColumnModel().getColumn(4).setPreferredWidth(100);
            all_customer_table.getColumnModel().getColumn(6).setPreferredWidth(50);
        }

        jPanel5.setOpaque(false);

        register_btn.setBackground(new java.awt.Color(0, 153, 51));
        register_btn.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        register_btn.setText("Save");
        register_btn.setFocusPainted(false);
        register_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                register_btnActionPerformed(evt);
            }
        });

        clear_btn.setBackground(new java.awt.Color(255, 255, 255));
        clear_btn.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        clear_btn.setText("Clear");
        clear_btn.setFocusPainted(false);
        clear_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_btnActionPerformed(evt);
            }
        });

        reactivate_btn.setBackground(new java.awt.Color(255, 255, 255));
        reactivate_btn.setText("<html>Reactivate Expired <br>    Season pass</html>");
        reactivate_btn.setFocusPainted(false);
        reactivate_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reactivate_btnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(reactivate_btn)
                    .addComponent(clear_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(register_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(register_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clear_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(reactivate_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel29.setText("ALL REGISTERED CUSTOMERS");

        jLabel36.setFont(new java.awt.Font("Tahoma", 3, 12)); // NOI18N
        jLabel36.setText("- Prepaid Customers");

        deleteBtn.setText("Delete Customer");
        deleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(qr_txt))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(26, 26, 26))
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel26)
                                                .addGap(14, 14, 14)))
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(level_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addComponent(vehicle_num_txt)))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(26, 26, 26)
                                        .addComponent(name_txt))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(id_txt, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(45, 45, 45))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel36)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(level_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(qr_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(id_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(name_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(vehicle_num_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(deleteBtn, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(204, 204, 255));

        level_switch_toggle_btn.setBackground(new java.awt.Color(51, 51, 255));
        level_switch_toggle_btn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        level_switch_toggle_btn.setForeground(new java.awt.Color(51, 51, 255));
        level_switch_toggle_btn.setText("Prepaid Customers");
        level_switch_toggle_btn.setFocusPainted(false);
        level_switch_toggle_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                level_switch_toggle_btnActionPerformed(evt);
            }
        });

        parking_qr_lbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        parking_qr_lbl.setForeground(new java.awt.Color(0, 0, 204));
        parking_qr_lbl.setText("QR code");

        parking_qr_txt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        parking_qr_txt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                parking_qr_txtKeyReleased(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(0, 0, 204));
        jLabel28.setText("ID");

        parking_id_txt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        parking_id_txt.setEnabled(false);
        parking_id_txt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                parking_id_txtKeyReleased(evt);
            }
        });

        reset_btn.setBackground(new java.awt.Color(255, 102, 102));
        reset_btn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        reset_btn.setText("Reset");
        reset_btn.setFocusPainted(false);
        reset_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset_btnActionPerformed(evt);
            }
        });

        park_unpark_btn.setBackground(new java.awt.Color(0, 153, 51));
        park_unpark_btn.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        park_unpark_btn.setText("Park");
        park_unpark_btn.setEnabled(false);
        park_unpark_btn.setFocusPainted(false);
        park_unpark_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                park_unpark_btnActionPerformed(evt);
            }
        });

        available_usage_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Vehicle No.", "Level", "Location No."
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        available_usage_table.setFocusable(false);
        available_usage_table.setGridColor(new java.awt.Color(204, 204, 204));
        available_usage_table.setRowHeight(20);
        available_usage_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(available_usage_table);
        if (available_usage_table.getColumnModel().getColumnCount() > 0) {
            available_usage_table.getColumnModel().getColumn(2).setPreferredWidth(100);
            available_usage_table.getColumnModel().getColumn(3).setPreferredWidth(50);
        }

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel30.setText("AVAILABLE PRKING USAGE");

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(0, 0, 204));
        jLabel31.setText("Name");

        jLabel33.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(0, 0, 204));
        jLabel33.setText("Vehivle No.");

        level_display_label.setBackground(new java.awt.Color(255, 153, 51));
        level_display_label.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        level_display_label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        level_display_label.setText("Visitor park");
        level_display_label.setOpaque(true);

        parking_name_txt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        parking_name_txt.setEnabled(false);

        parking_veh_num_txt.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        parking_veh_num_txt.setEnabled(false);

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(0, 0, 204));
        jLabel34.setText("Location");

        location_num_combo_box.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        location_num_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No. 1", "No. 2", "No. 3", " " }));
        location_num_combo_box.setFocusable(false);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rsz_mp-51.png"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parking_qr_lbl, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(parking_id_txt)
                        .addComponent(parking_name_txt)
                        .addComponent(parking_veh_num_txt)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addComponent(level_display_label, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(location_num_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(reset_btn)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(park_unpark_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(parking_qr_txt, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(level_switch_toggle_btn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(23, 23, 23))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(level_switch_toggle_btn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parking_qr_lbl)
                    .addComponent(parking_qr_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(parking_id_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parking_name_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(parking_veh_num_txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(level_display_label, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34)))
                    .addComponent(location_num_combo_box))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reset_btn)
                    .addComponent(park_unpark_btn))
                .addGap(23, 23, 23)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout main_panelLayout = new javax.swing.GroupLayout(main_panel);
        main_panel.setLayout(main_panelLayout);
        main_panelLayout.setHorizontalGroup(
            main_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(main_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        main_panelLayout.setVerticalGroup(
            main_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(main_panelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(main_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(main_panelLayout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(main_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(main_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void level_switch_toggle_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_level_switch_toggle_btnActionPerformed
        // TODO add your handling code here:
        boolean selected = level_switch_toggle_btn.isSelected();
        if(selected){
            // initialize the interfaces
            level_switch_toggle_btn.setText("Visitors");
            this.isPrepaidCustomer = false;
            park_unpark_btn.setEnabled(true);
            
        }else{
            // initialize the interfaces
            level_switch_toggle_btn.setText("Prepaid Customers");
            this.isPrepaidCustomer = true;
            park_unpark_btn.setEnabled(false);
            
        }
        reset();
    }//GEN-LAST:event_level_switch_toggle_btnActionPerformed

    private void register_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_register_btnActionPerformed
        // TODO add your handling code here:
        String qr = qr_txt.getText().trim();
        String id = id_txt.getText().trim();
        String name = name_txt.getText().trim();
        String veh_num = vehicle_num_txt.getText().trim();
        int level = level_combo_box.getSelectedIndex();
       
        
        if (qr.isEmpty() || id.isEmpty() || name.isEmpty() || veh_num.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please complete the all information to registration.","Message",JOptionPane.ERROR_MESSAGE);
        }else{
             //now we have all the informations
             //save them to database
             
            String query="INSERT INTO customer(qr_code,id_number,name,veh_num,level,renew_date) VALUES("
                    +"'"+qr+"'"+","
                    +"'"+id+"'"+","
                    +"'"+name+"'"+","
                    +"'"+veh_num+"'"+","
                    +""+level+","
                    +"'"+getRenewDate()+"'"
                    +")";
           
                
            boolean setQuery = connect.setQuery(query);
            if(setQuery){
                JOptionPane.showMessageDialog(null, "Customer registered successfully !", "Message", JOptionPane.INFORMATION_MESSAGE);
                clearRegForm();
                retrieveAllCustomers();
            }

        }
    }//GEN-LAST:event_register_btnActionPerformed

    private void clear_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_btnActionPerformed
        // TODO add your handling code here:
        clearRegForm();
    }//GEN-LAST:event_clear_btnActionPerformed

    private void reactivate_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reactivate_btnActionPerformed
        // TODO add your handling code here:
        int selectedRowCount = all_customer_table.getSelectedRowCount();
        if (selectedRowCount==0) {
            JOptionPane.showMessageDialog(null, "Select a customer to reactivate the expired pass", "Message", JOptionPane.ERROR_MESSAGE);
        }else{
            int selectedRow = all_customer_table.getSelectedRow();
            String status = all_customer_dtm.getValueAt(selectedRow, 6).toString();
            if(!status.equals("Expired")){
                String renewDate = all_customer_dtm.getValueAt(selectedRow, 5).toString();
                JOptionPane.showMessageDialog(null, "Do not need reactivate. It is upto date to "+renewDate, "Message", JOptionPane.INFORMATION_MESSAGE);
            }else{
                String name = all_customer_dtm.getValueAt(selectedRow, 2).toString();
                // ask from the user to update the expire date
                int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you need to reactivate expired season pass of "+name+" ?", "Message", JOptionPane.YES_NO_OPTION);
                if(showConfirmDialog==0){
                    // if yes         
                    // activate the pass to one month
                    int index = Integer.parseInt(all_customer_dtm.getValueAt(selectedRow, 0).toString());
                    String query="update customer set "+"renew_date='"+getRenewDate()+"'"+" WHERE serial_no="+index+"";

                    boolean setQuery = connect.setQuery(query);
                    if(setQuery){
                        JOptionPane.showMessageDialog(null, "Reactivated the expire date successfully !", "Message", JOptionPane.INFORMATION_MESSAGE);
                        retrieveAllCustomers();
                    }
                }

            }
        }
    }//GEN-LAST:event_reactivate_btnActionPerformed

    private void reset_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reset_btnActionPerformed
        // TODO add your handling code here:
        reset();
    }//GEN-LAST:event_reset_btnActionPerformed

    private void parking_qr_txtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_parking_qr_txtKeyReleased
        // TODO add your handling code here:
        if(evt.getKeyChar()=='\n'){
            String qr = parking_qr_txt.getText().trim();
            
            if(qr.isEmpty()){
                JOptionPane.showMessageDialog(null, "Please scann the QR code", "Message", JOptionPane.INFORMATION_MESSAGE);
            
            }else{
                // now we have qr the retrive the details from database
                String query="SELECT * FROM customer WHERE qr_code='"+qr+"'";
             
                ResultSet r=connect.getQuery(query);
                try {
                    int count = 0;
                    while(r.next()){
                        count++;
                        String id=r.getString("id_number");
                        String name=r.getString("name");
                        String veh_num=r.getString("veh_num");
                        String renew_date = r.getString("renew_date");
                        
                        int parking = r.getInt("parking");
                        //convert int level value to string word
                        int level = r.getInt("level");
                        

                        parking_id_txt.setText(id);
                        parking_name_txt.setText(name);
                        parking_veh_num_txt.setText(veh_num);
                        setLevelColor(level);
                        
                        if(isExpired(renew_date)){
                            JOptionPane.showMessageDialog(null, "Expired QR code.Please reactivate", "Message", JOptionPane.INFORMATION_MESSAGE);
                            reset();
                        }else{
                            // if the vehicle is parked
                            if (parking==1) {

                                int loc_num = r.getInt("location_num");
                                retriveAllLocationsToCombo();
                                location_num_combo_box.setSelectedIndex(loc_num-1);
                                location_num_combo_box.setEnabled(false);


                                parkPrepaidCustomers(false, qr,-1);
                                parking_qr_txt.setText("");
                                parking_qr_txt.requestFocus();
                                //reset();

                            }else{

                                retriveAvailableLocationsToCombo(level);
                                String qr_ = parking_qr_txt.getText().trim();
                                String id_ = parking_id_txt.getText().trim();

                                if (qr_.isEmpty() && id_.isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Please Scan the QR code.", "Message", JOptionPane.INFORMATION_MESSAGE);
                                }else if(location_combo_model.getSize()==0){
                                    JOptionPane.showMessageDialog(null, level_display_label.getText()+" is Full", "Message", JOptionPane.INFORMATION_MESSAGE);


                                }else{
                                    String selected=location_num_combo_box.getSelectedItem().toString();

                                    parkPrepaidCustomers(true, qr_,getLocation(selected));
                                    parking_qr_txt.setText("");
                                    parking_qr_txt.requestFocus();
                                    //reset();
                                }

                            }
                        
                        }
                        
                        
                        
                    }
                    if(count==0){
                        JOptionPane.showMessageDialog(null, "Unregistered QR code", "Message", JOptionPane.INFORMATION_MESSAGE);
                        reset();
                    }
                } catch (SQLException ex) {}
            }
            updateDashBoard();
            retriveCurrentUsage();
        }
    }//GEN-LAST:event_parking_qr_txtKeyReleased

    private void park_unpark_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_park_unpark_btnActionPerformed
        // TODO add your handling code here:
        String text = park_unpark_btn.getText();
        if(text.equals("Park")){
            
            if(isPrepaidCustomer){
                
                
                
            }else{
                
                String id = parking_id_txt.getText().trim();
                String name = parking_name_txt.getText().trim();
                String veh_num = parking_veh_num_txt.getText().trim();
                if (name.isEmpty() || id.isEmpty() || veh_num.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please complete all informations", "Message", JOptionPane.INFORMATION_MESSAGE);
                }else if(location_combo_model.getSize()==0){
                 
                    JOptionPane.showMessageDialog(null, level_display_label.getText()+" is Full", "Message", JOptionPane.INFORMATION_MESSAGE);
                }else if(isRegistered(id)){
                    
                    JOptionPane.showMessageDialog(null, "ID number is already registered.", "Message", JOptionPane.INFORMATION_MESSAGE);
                    
                }else{
                    // insert to database
                    
                     //now we have all the informations
                     //save them to database
                    String selected=location_num_combo_box.getSelectedItem().toString();

                    String query="INSERT INTO customer(id_number,name,veh_num,level,renew_date,parking,location_num) VALUES("
                            +"'"+id+"'"+","
                            +"'"+name+"'"+","
                            +"'"+veh_num+"'"+","
                            +"2,"
                            +"'-',"
                            +"1,"
                            +""+getLocation(selected)+""
                            +")";


                    boolean setQuery = connect.setQuery(query);
                    if(setQuery){
                        JOptionPane.showMessageDialog(null, "The Visitor's vehicle parked successfully !", "Message", JOptionPane.INFORMATION_MESSAGE);
                        reset();
                        retrieveAllCustomers();
                    }

                    
                }
              
            }
        }else if(text.equals("Remove")){
            if(isPrepaidCustomer){
                
            }else{
                // remove from database
                String id = parking_id_txt.getText();
                removeVisitor(id);
                reset();
            }
        }
        updateDashBoard();
        retriveCurrentUsage();
        
    }//GEN-LAST:event_park_unpark_btnActionPerformed

    private void parking_id_txtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_parking_id_txtKeyReleased
        // TODO add your handling code here:
        if(evt.getKeyChar()=='\n'){
            String id = parking_id_txt.getText().trim();
            
            if(id.isEmpty()){
                JOptionPane.showMessageDialog(null, "Please enter the visitor's ID", "Message", JOptionPane.INFORMATION_MESSAGE);
            }else{
                // now we have id then retrive the details from database
                String query="SELECT * FROM customer WHERE id_number='"+id+"'";
             
                ResultSet r=connect.getQuery(query);
                try {
                    int count = 0;
                    while(r.next()){
                        count++;
                        
                        String name=r.getString("name");
                        String veh_num=r.getString("veh_num");
                        
                        
                        int parking = r.getInt("parking");
                        //convert int level value to string word
                        int level = r.getInt("level");
                        
                        parking_name_txt.setText(name);
                        parking_veh_num_txt.setText(veh_num);
                        setLevelColor(level);
                        // if the vehicle is parked
                        if (parking==1) {
                            int loc_num = r.getInt("location_num");
                            retriveAllLocationsToCombo();
                            location_num_combo_box.setSelectedIndex(loc_num-1);
                            location_num_combo_box.setEnabled(false);
                            park_unpark_btn.setText("Remove");
                        }else{
                            retriveAvailableLocationsToCombo(level);
                            park_unpark_btn.setText("Park");
                        }
                        
                        parking_id_txt.setEnabled(false);
                        parking_name_txt.setEnabled(false);
                        parking_veh_num_txt.setEnabled(false);
                        
                        //addCustomerToTable(index,id, name, veh_num, level,renew_date,status);
                    }
                    if(count==0)
                        JOptionPane.showMessageDialog(null, "Unregistered visitor ID", "Message", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {}
            }
        }
    }//GEN-LAST:event_parking_id_txtKeyReleased

    private void qr_txtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_qr_txtKeyReleased
        // TODO add your handling code here:
         if(evt.getKeyChar()=='\n'){
             String qr = qr_txt.getText().trim();
             if (isIssued(qr)) {
                 JOptionPane.showMessageDialog(null, "QR code is already issued.", "Message", JOptionPane.INFORMATION_MESSAGE);
                 qr_txt.setText("");
             }
         }
    }//GEN-LAST:event_qr_txtKeyReleased

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
        // TODO add your handling code here:
        int selectedRowCount = all_customer_table.getSelectedRowCount();
        if (selectedRowCount==0) {
            JOptionPane.showMessageDialog(null, "Select a customer to remove", "Message", JOptionPane.INFORMATION_MESSAGE);
        }else{
            int selectedRow = all_customer_table.getSelectedRow();
            String index = all_customer_dtm.getValueAt(selectedRow, 0).toString();
            if(isParked(Integer.parseInt(index))){
                
                JOptionPane.showMessageDialog(null, "Already Parked. Cannot delete parked vehicles", "Message", JOptionPane.INFORMATION_MESSAGE);
            }else{
                String name = all_customer_dtm.getValueAt(selectedRow, 2).toString();
                // ask from the user to delete the customer
                int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you need to delete the customer, "+name+" ?", "Message", JOptionPane.YES_NO_OPTION);
                if(showConfirmDialog==0){
                    // if yes         
                    // delete the custmer
                    
                    String query="DELETE FROM customer WHERE serial_no="+index+"";

                    boolean setQuery = connect.setQuery(query);
                    if(setQuery){
                        JOptionPane.showMessageDialog(null, "Delete customer successfully !", "Message", JOptionPane.INFORMATION_MESSAGE);
                        retrieveAllCustomers();
                    }
                }

            }
        }
    }//GEN-LAST:event_deleteBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable all_customer_table;
    private javax.swing.JTable available_usage_table;
    private javax.swing.JButton clear_btn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JTextField id_txt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel level1_parking_count_lbl;
    private javax.swing.JLabel level2_parking_count_lbl;
    private javax.swing.JLabel level3_parking_count_lbl;
    private javax.swing.JComboBox<String> level_combo_box;
    private javax.swing.JLabel level_display_label;
    private javax.swing.JToggleButton level_switch_toggle_btn;
    private javax.swing.JComboBox<String> location_num_combo_box;
    private javax.swing.JPanel main_panel;
    private javax.swing.JTextField name_txt;
    private javax.swing.JButton park_unpark_btn;
    private javax.swing.JTextField parking_id_txt;
    private javax.swing.JTextField parking_name_txt;
    private javax.swing.JLabel parking_qr_lbl;
    private javax.swing.JTextField parking_qr_txt;
    private javax.swing.JTextField parking_veh_num_txt;
    private javax.swing.JTextField qr_txt;
    private javax.swing.JButton reactivate_btn;
    private javax.swing.JButton register_btn;
    private javax.swing.JButton reset_btn;
    private javax.swing.JTextField vehicle_num_txt;
    // End of variables declaration//GEN-END:variables
}
