package com.company;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main extends JFrame {
    private static final long serialVersionUID = 1L;

    protected static final int INFORMATION_MESSAGE = 0;

    static Main main;

    JPanel infoPanel , eventPanel;
    JTable infoTable , eventTable;
    Object[][] infoList , eventList;
    JScrollPane infoListScr , conPane , eventListScr , eventPane;
    DefaultTableModel infomodel , eventmodel;

    String[] info_Column = { "대원코드", "대원이름" , "연결 상태" };
    String[] event_Column = { "대원코드", "X좌표" , "Y좌표" , "온도" , "이벤트" } ;

    int map_clickX , map_clickY;
    JButton mapPanel_btn;
    MappingPanel map_panel;

    Thread dataSelect_thread , membersSelect_thread , timeSelect_thread;

    ArrayList<Members> fieldMember_List = new ArrayList<Members>();
    ArrayList<Members> totalMember_List = new ArrayList<Members>();

    boolean isClicked_mapPanel = false;
    boolean isClicked_mapBtn = false;
    boolean delete_dataTable = true;

    String membersDB_url = "jdbc:mysql://10.30.90.255/Members";
    String dataDB_url = "jdbc:mysql://10.30.90.255/Data";
    String updateDB_url = "jdbc:mysql://10.30.90.255/Update";

    class MappingPanel extends JPanel
    {
        private static final long serialVersionUID = 1L;

        Image panelImage;
        Ellipse2D.Double ellipse;

        public MappingPanel()
        {
            panelImage = Toolkit.getDefaultToolkit().getImage("./현장Map.PNG");
            ellipse = new Ellipse2D.Double();
        }

        public Image getBackgroundImage() { return panelImage; }

        public int setEllipseSize( double temperature )
        {
            if( temperature > 0 && temperature < 100 )
            {
                return 5;
            }
            else if( temperature >= 100 && temperature < 300 )
            {
                return 10;
            }
            else if( temperature >= 300 && temperature < 700 )
            {
                return 15;
            }
            else if( temperature >= 700 )
            {
                return 20;
            }

            //영하로 떨어지는 경우
            return 5;
        }

        public void paint(Graphics g)
        {
            super.paint(g);

            Graphics2D g2 = (Graphics2D) g;
            g.drawImage(panelImage, 5 , 20 , 490 , 500, this);

            if (isClicked_mapPanel)
            {
                g.setColor(Color.BLUE);
                g.fill3DRect(map_clickX, map_clickY, 10, 10, false);

                if (fieldMember_List.size() > 0 && isClicked_mapBtn == true)
                {
                    for (int j = 0; j < fieldMember_List.size(); ++j)
                    {
                        double temperature = fieldMember_List.get(j).getTemp();

                        ellipse.x = fieldMember_List.get(j).getX();
                        ellipse.y = fieldMember_List.get(j).getY();

                        int size = setEllipseSize( temperature );

                        ellipse.height = size;
                        ellipse.width = size;

                        if( fieldMember_List.get(j).getConnectState() == 0 )
                            g2.setColor(Color.RED);
                        else
                            g2.setColor(Color.GREEN);


                        if( ellipse.x < 5 )
                        {
                            ellipse.x = 5;
                        }
                        else if ( ellipse.x > 490 )
                        {
                            ellipse.x = 490 - ellipse.width ;
                            g2.drawString(fieldMember_List.get(j).getID() + "," + fieldMember_List.get(j).getTemp(), (int) ellipse.x - 28, (int)ellipse.y );

                        }
                        else if ( ellipse.x > 500 )
                        {
                            ellipse.x =  500 - ellipse.height;
                        }

                        if( ellipse.y < 20 )
                        {
                            ellipse.y = 20;
                            g2.drawString(fieldMember_List.get(j).getID() + "," + fieldMember_List.get(j).getTemp(),
                                    (int) ellipse.x - 20,(int)ellipse.y + 30);
                        }
                        else if ( ellipse.y > 520 )
                        {
                            ellipse.y = 520 - ellipse.height;
                        }
                        else if ( ellipse.y > 490 )
                        {
                            ellipse.y = 490 - ellipse.width;
                        }

                        if( ellipse.y > 20 && ellipse.x < 490 )
                        {
                            g2.drawString(fieldMember_List.get(j).getID() + "," + fieldMember_List.get(j).getTemp(), (int) ellipse.x,(int) ellipse.y);
                        }

                        g2.fill(ellipse);
                    }
                }
            }
            else
            {
                return;
            }
        }
    }
    private void setPosition(ResultSet srs, int count , String col1, String col2)
            throws UnsupportedEncodingException, SQLException
    {
        double xPosition = fieldMember_List.get(count).getX();
        double yPosition = fieldMember_List.get(count).getY();

        System.out.println("xPosition  /  yPsotion"  +  xPosition  + " /  " + yPosition);

        int rCount = 0;

        while (srs.next())
        {

            if (!col1.equals("") && !col2.equals(""))
            {
                String dis = new String(srs.getString("Distance").getBytes("ISO-8859-1"));
                String ang = new String(srs.getString("Angle").getBytes("ISO-8859-1"));
                String temp = new String(srs.getString("Temperature").getBytes("ISO-8859-1"));
                String emergencyState = new String(srs.getString("Emergency").getBytes("ISO-8859-1"));

                if( Double.parseDouble(ang) < -6.5 || Double.parseDouble(ang) > 6.5 ||
                        Double.parseDouble(temp) < 0 || Double.parseDouble(temp) > 1024 ||
                        Double.parseDouble(dis) < 0 || Double.parseDouble(dis) > 3 )
                {
                    ++rCount;
                }
                else
                {
                    if( rCount >= fieldMember_List.get(count).getRowCount())
                    {
                        fieldMember_List.get(count).setAngle(Double.parseDouble(ang));
                        fieldMember_List.get(count).setEmergencyState(Integer.parseInt(emergencyState));

                        xPosition = fieldMember_List.get(count).getX() + 15*Double.parseDouble(dis)*Math.sin(fieldMember_List.get(count).getAngle());
                        yPosition = fieldMember_List.get(count).getY() - 15*Double.parseDouble(dis)*Math.cos(fieldMember_List.get(count).getAngle());

                        if( fieldMember_List.get(count).getEmergencyState() == 1)
                        {
                            System.out.println(fieldMember_List.get(count).getID() + "( " + fieldMember_List.get(count).getName() + " )"
                                    + "님이 긴급 호출 버튼을 누르셨습니다! ");
                            eventmodel.setValueAt("Help!", count, 4);
                        }
                        else
                        {
                            eventmodel.setValueAt("", count, 4);
                        }

                        ++rCount;

                        if( !srs.next() )
                        {
                            long t = System.currentTimeMillis();

                            SimpleDateFormat dayTime = new SimpleDateFormat("hhmmss");
                            String str = dayTime.format(new Date(t));

                            Connection updateDB_Connection = DriverManager.getConnection(updateDB_url, "root", "apmsetup");
                            Statement statement = (Statement) updateDB_Connection.createStatement();

                            statement.executeUpdate("insert into t" + fieldMember_List.get(count).getID()+ " values("+str+")");
                            System.out.println("insert into t" + fieldMember_List.get(count).getID()+ " values("+str+")");

                            fieldMember_List.get(count).setTemp(Double.parseDouble(temp));
                            fieldMember_List.get(count).setLocation(xPosition, yPosition);
                            fieldMember_List.get(count).setRowCount(rCount);
                            fieldMember_List.get(count).setUnrenewCount(0);

                            eventmodel.setValueAt(fieldMember_List.get(count).getX(), count, 1);
                            eventmodel.setValueAt(fieldMember_List.get(count).getY(), count, 2);
                            eventmodel.setValueAt(fieldMember_List.get(count).getTemp(), count, 3);

                            if( fieldMember_List.get(count).getConnectState() == 0)
                            {
                                fieldMember_List.get(count).setConnectState(1);

                                infomodel.setValueAt("On", count , 2);
                                map_panel.repaint();
                            }
                        }
                    }
                    else
                    {
                        ++rCount;
                    }
                }
            }
        }

        System.out.println("( x , y ) - > ( " + fieldMember_List.get(count).getX() + "  ,  " + fieldMember_List.get(count).getY()  + " )");
    }

    public void receiveData()
    {
        Connection dataDB_Connection;
        Statement statement = null;

        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            dataDB_Connection = DriverManager.getConnection(dataDB_url, "root", "apmsetup");

            statement = (Statement) dataDB_Connection.createStatement();

            System.out.println("==================== Data DB Connection Success! ======================");

            for(int i = 0 ; i < fieldMember_List.size(); ++i)
            {
                System.out.println("select * from d"+fieldMember_List.get(i).getID());
                ResultSet srs = statement.executeQuery("select * from d"+fieldMember_List.get(i).getID());

                try
                {
                    setPosition(srs, i , "X", "Y");
                }
                catch (UnsupportedEncodingException e1)
                {
                    e1.printStackTrace();
                }

                if( i == fieldMember_List.size()-1 )
                {
                    map_panel.repaint();;
                }
            }
            dataDB_Connection.close();
        }
        catch (ClassNotFoundException e1)
        {
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }
    }


    private void InfoTable() {
        infoTable = new JTable(infomodel);

        infoTable.getColumn("대원코드").setPreferredWidth(100);
        infoTable.getColumn("대원이름").setPreferredWidth(100);

        DefaultTableCellRenderer infoCellch = new DefaultTableCellRenderer();
        infoCellch.setHorizontalAlignment(SwingConstants.CENTER);

        TableColumnModel infoTabData = infoTable.getColumnModel();

        for (int i = 0; i < infoTabData.getColumnCount(); i++) {
            infoTabData.getColumn(i).setCellRenderer(infoCellch);
        }

        infoTable.getTableHeader().setReorderingAllowed(false);
        setVisible(true);
    }

    private void EventTable() {
        eventTable = new JTable(eventmodel);

        eventTable.getColumn("대원코드").setPreferredWidth(100);
        eventTable.getColumn("X좌표").setPreferredWidth(100);
        eventTable.getColumn("Y좌표").setPreferredWidth(100);
        eventTable.getColumn("이벤트").setPreferredWidth(100);

        DefaultTableCellRenderer eventCellch = new DefaultTableCellRenderer();
        eventCellch.setHorizontalAlignment(SwingConstants.CENTER);

        TableColumnModel eventTabData = eventTable.getColumnModel();

        for (int i = 0; i < eventTabData.getColumnCount(); i++) {
            eventTabData.getColumn(i).setCellRenderer(eventCellch);
        }

        eventTable.getTableHeader().setReorderingAllowed(false);
        setVisible(true);
    }

    private void LayoutLoad()
    {
        infoPanel = new JPanel();
        infoPanel.setLayout(null);
        infoPanel.setBounds(0, 20, 350, 250);

        eventPanel = new JPanel();
        eventPanel.setLayout(null);
        eventPanel.setBounds(0, 300, 350, 250);

        conPane = new JScrollPane(infoTable);
        conPane.setBounds(20, 130, 270, 300);
        setLayout(null);

        infoList = new Object[fieldMember_List.size()][info_Column.length];
        infomodel = new DefaultTableModel(infoList, info_Column);
        infoPanel.setBorder(new TitledBorder("현장 대원 목록"));
        InfoTable();

        infoListScr = new JScrollPane(infoTable);
        infoListScr.setBounds(10, 20, 330, 220);
        infoListScr.setPreferredSize(new Dimension(369, 203));

        eventPane = new JScrollPane(eventTable);
        eventPane.setBounds(20, 410, 270, 300);
        setLayout(null);

        eventList = new Object[fieldMember_List.size()][event_Column.length];
        eventmodel = new DefaultTableModel(eventList, event_Column);
        eventPanel.setBorder(new TitledBorder("대원 이벤트 목록"));
        EventTable();

        eventListScr = new JScrollPane(eventTable);
        eventListScr.setBounds(10,20,330,220);
        eventListScr.setPreferredSize(new Dimension(369, 203));

        map_panel = new MappingPanel();
        map_panel.setLayout(null);
        map_panel.setBounds(370, 20, 500 , 530);
        map_panel.setBorder(new TitledBorder("현장 Map"));

        mapPanel_btn = new JButton("활성화");
        mapPanel_btn.setBounds(875, 30, 75, 50);

        map_panel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                if (isClicked_mapBtn) {
                    JOptionPane.showMessageDialog(null, "맵 버튼을 비활성화 하세요!", "ERROR", INFORMATION_MESSAGE);
                    return;
                } else {
                    isClicked_mapPanel = true;

                    map_clickX = e.getX();
                    map_clickY = e.getY();

                    if (fieldMember_List.size() > 0) {
                        for (int i = 0; i < fieldMember_List.size(); ++i) {
                            fieldMember_List.get(i).setLocation(map_clickX, map_clickY);
                            if (eventmodel.getRowCount() > 0) {
                                eventmodel.setValueAt(fieldMember_List.get(i).getX(), i, 1);
                                eventmodel.setValueAt(fieldMember_List.get(i).getY(), i, 2);
                            }
                        }
                    }
                    map_panel.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }
        });

        mapPanel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isClicked_mapPanel) {
                    JOptionPane.showMessageDialog(null, "시작점을 클릭하지 않았습니다!", "ERROR", INFORMATION_MESSAGE);
                    return;
                }
                if (fieldMember_List.size() == 0) {
                    JOptionPane.showMessageDialog(null, "등록된 대원이 존재하지 않습니다", "ERROR", INFORMATION_MESSAGE);
                    return;
                }

                isClicked_mapBtn = !isClicked_mapBtn;

                Connection dataDB_Connection;
                Statement statement;

                if (delete_dataTable) {
                    delete_dataTable = !delete_dataTable;

                    try {
                        Class.forName("com.mysql.jdbc.Driver");

                        dataDB_Connection = DriverManager.getConnection(dataDB_url, "root", "apmsetup");

                        statement = (Statement) dataDB_Connection.createStatement();

                        for (int i = 0; i < fieldMember_List.size(); ++i) {
                            System.out.println("delete from d" + fieldMember_List.get(i).getID());

                            statement.executeUpdate("delete from d" + fieldMember_List.get(i).getID());
                            fieldMember_List.get(i).setLocation(map_clickX, map_clickY);
                        }

                        dataDB_Connection.close();
                        map_panel.repaint();
                    } catch (ClassNotFoundException e1) {
                    } catch (SQLException e1) {
                    }
                }

                dataSelect_thread = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            if (isClicked_mapBtn) {
                                System.out.println("dataSelect_thread doing work..");
                                receiveData();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    System.out.println("error receive");
                                }
                            } else {
                                System.out.println("dataSelect_thread stop work..");
                                break;
                            }
                        }
                    }
                });

                timeSelect_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection updateDB_Connection = null;
                        Statement statement = null;

                        try {
                            updateDB_Connection = DriverManager.getConnection(updateDB_url, "root", "apmsetup");
                            statement = (Statement) updateDB_Connection.createStatement();
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        System.out.println("==================== Update DB Connection Success! ======================");

                        while (true) {
                            System.out.println("timeSelect_thread working.....");

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }

                            if (isClicked_mapBtn) {
                                for (int i = 0; i < fieldMember_List.size(); ++i) {
                                    try {
                                        ResultSet srs = statement
                                                .executeQuery("select * from t" + fieldMember_List.get(i).getID());
                                        System.out.println("select * from t" + fieldMember_List.get(i).getID());

                                        while (srs.next()) {

                                            String time = new String(srs.getString("time").getBytes("8859_1"),
                                                    "KSC5601");

                                            if (Integer.parseInt(time) == fieldMember_List.get(i).getCurrentTime())
                                            {
                                                fieldMember_List.get(i).setUnrenewCount(fieldMember_List.get(i).getUnrenewCount() + 1);

                                                if (fieldMember_List.get(i).getConnectState() == 0) {
                                                    infomodel.setValueAt("Off", i, 2);
                                                    map_panel.repaint();
                                                }
                                            } else {
                                                fieldMember_List.get(i).setCurrentTime(Integer.parseInt(time));
                                            }
                                        }
                                    } catch (SQLException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                for (int i = 0; i < fieldMember_List.size(); ++i) {
                                    fieldMember_List.get(i).setUnrenewCount(0);
                                }
                                System.out.println("timeSelect_thread is dead....");
                                break;
                            }
                        }
                    }
                });

                dataSelect_thread.start();
                timeSelect_thread.start();
            }
        });

        infoPanel.add(infoListScr);
        eventPanel.add(eventListScr);

        add(infoPanel);
        add(eventPanel);
        add(map_panel);
        add(mapPanel_btn);
    }

    private void MenubarLoad()
    {
        JMenuBar menuBar = new JMenuBar();

        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("파일(F)");
        menuBar.add(fileMenu);
        fileMenu.setMnemonic('F');

        JMenuItem newMenu = new JMenuItem("새로하기(N)");
        fileMenu.add(newMenu);
        newMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (isClicked_mapBtn) {
                    isClicked_mapBtn = !isClicked_mapBtn;
                }

                main.setVisible(false);
                main = new Main();
            }
        });

        JMenuItem closeMenu = new JMenuItem("종료(E)");

        fileMenu.add(closeMenu);

        closeMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK ^ InputEvent.SHIFT_MASK));
        closeMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
    private void setDesign()
    {
        //this.setLayout(new BorderLayout());

        this.MenubarLoad();

        this.LayoutLoad();
    }

    public Main()
    {
        super("FireRangers");


        this.setDesign();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(0, 0, 970, 650);
        this.setVisible(true);

        Connection memberDB_connection = null;
        Statement statement = null;

        try
        {
            Class.forName("com.mysql.jdbc.Driver");

            memberDB_connection = DriverManager.getConnection(membersDB_url, "root", "apmsetup");
            statement = (Statement) memberDB_connection.createStatement();

            ResultSet srs = null;

            System.out.println("==================== Members DB Connection Success! ======================");

            srs = statement.executeQuery("select * from Gwacheon");

            System.out.println("select * from Gwacheon");
            System.out.println("These people is in Gwacheon fire station");

            while( srs.next() )
            {
                String id = new String(srs.getString("ID").getBytes("8859_1"),"KSC5601");
                String name = new String(srs.getString("Name").getBytes("8859_1"),"KSC5601");

                totalMember_List.add(new Members(id,name));

                System.out.println(totalMember_List.get(totalMember_List.size()-1).getID() + "  /  " +
                        totalMember_List.get(totalMember_List.size()-1).getName() );
            }

            memberDB_connection.close();

        }
        catch (ClassNotFoundException e1)
        {
        }
        catch (SQLException e1)
        {
            e1.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        membersSelect_thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Connection dataDB_Connection = null;
                Statement statement = null;

                try
                {
                    dataDB_Connection = DriverManager.getConnection(dataDB_url, "root", "apmsetup");
                    statement = (Statement) dataDB_Connection.createStatement();
                }
                catch (SQLException e2)
                {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }


                System.out.println("==================== Data DB Connection Success! ======================");

                while(true)
                {
                    if(!isClicked_mapPanel)
                    {
                        try
                        {
                            Thread.sleep(500);
                            ResultSet srs = null;

                            for(int i = 0 ; i < totalMember_List.size(); ++i)
                            {
                                srs = statement.executeQuery("select * from d"+totalMember_List.get(i).getID());
                                System.out.println("select * from d"+totalMember_List.get(i).getID());

                                if(!srs.next())
                                {
                                    System.out.println(totalMember_List.get(i).getID() +"( " +
                                            totalMember_List.get(i).getName() + " ) "+ " 의 데이터가 비어 있습니다");
                                }
                                else
                                {
                                    if( fieldMember_List.size() > 0)
                                    {
                                        boolean checkList = false;

                                        for(int j = 0 ; j < fieldMember_List.size(); ++j)
                                        {
                                            if(fieldMember_List.get(j).getID() == totalMember_List.get(i).getID() &&
                                                    fieldMember_List.get(j).getName() == totalMember_List.get(i).getName())
                                            {
                                                checkList = true;
                                                break;
                                            }
                                        }

                                        if( !checkList )
                                        {
                                            System.out.println(totalMember_List.get(i).getID() +"( " +
                                                    totalMember_List.get(i).getName() + " ) " + " 님을 추가합니다");
                                            fieldMember_List.add(new Members(totalMember_List.get(i).getID(), totalMember_List.get(i).getName()));
                                        }
                                    }
                                    else
                                    {
                                        System.out.println(totalMember_List.get(i).getID() +"( " +
                                                totalMember_List.get(i).getName() + " ) " + " 님을 추가합니다");
                                        fieldMember_List.add(new Members(totalMember_List.get(i).getID(), totalMember_List.get(i).getName()));
                                    }
                                }
                            }
                        }
                        catch (SQLException e1)
                        {
                            e1.printStackTrace();
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        System.out.println("membersSelect_thread Closing...");
                        break;
                    }
                }

                try
                {
                    dataDB_Connection.close();
                }
                catch (SQLException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for(int i = 0 ; i < fieldMember_List.size() ; ++i)
                {
                    System.out.println( i + "번째 대원  : " + fieldMember_List.get(i).getID());
                    infomodel.insertRow(i, new Object[]{fieldMember_List.get(i).getID(), fieldMember_List.get(i).getName() , "On"});
                    eventmodel.insertRow(i, new Object[]{fieldMember_List.get(i).getID(),fieldMember_List.get(i).getX() , fieldMember_List.get(i).getY()});
                }
            }
        });

        membersSelect_thread.start();
    }

    public static void main(String[] args)
    {
        main = new Main();
    }
}
