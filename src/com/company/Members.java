package com.company;
import java.util.ArrayList;
import java.util.Comparator;

public class Members
{
    double x,y,ang,temp;
    int rowCount , unrenewCount , currentTime , connectState  , emergencyState;

    String id;
    String name;

    Members(String id, String S_name)
    {
        this.id = id;
        this.name = S_name;

        rowCount = 0;
        unrenewCount = 0;
        connectState = 1;
        emergencyState = 0;
    }

    public int getRowCount() { return rowCount; }
    public int getUnrenewCount() { return unrenewCount; }
    public int getConnectState() { return connectState; }
    public int getCurrentTime() { return currentTime; }
    public int getEmergencyState() { return emergencyState; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return ang; }
    public double getTemp() { return temp; }

    public String getID() { return id; }
    public String getName() { return name; }


    public void setRowCount( int count )
    {
        if( count < 0)
            return ;

        this.rowCount = count;
    }
    public void setUnrenewCount( int count )
    {
        if( count > 15)
        {
            this.connectState = 0;

            System.out.println(this.id + " ( " + this.name + " ) " + "님의 통신이 끊겼습니다.");
            System.out.println("count : " + count + " 현재 연결상태  : " + this.connectState);

        }
        else
        {
            this.unrenewCount = count;
            System.out.println("Current count : " + this.unrenewCount);
        }
    }
    public void setConnectState( int state )
    {
        if( state != 0 && state != 1)
            return ;

        this.connectState = state;
    }
    public void setCurrentTime( int time )
    {
        if( this.currentTime > time )
            return ;

        this.currentTime = time;
    }
    public void setEmergencyState( int state )
    {
        if( state != 0 && state != 1)
            return ;

        this.emergencyState = state;
    }

    public void setLocation(double xPosition , double yPosition)
    {
        this.x = xPosition;
        this.y = yPosition;
    }
    public void setAngle(double angle)
    {
        ang += angle;
        setAngleRange();
    }
    public void setTemp(double temp)
    {
        this.temp = temp;
    }

    public void setID( String id)
    {
        if( id == "" )
            return;

        this.id = id;
    }
    public void setName( String name )
    {
        if( name == "" )
            return ;

        this.name = name;
    }
    public void setAngleRange()
    {
        if( ang > 6.28)
            ang -= 6.28;
        else if( ang < -6.28)
            ang += 6.28;

        if(ang>=0.5&&ang<=1.3)
            ang=0.79;
        else if(ang>1.3&&ang<2.0)
            ang=1.57;
        else if(ang>=2.0&&ang<2.7)
            ang=2.37;
        else if(ang>=2.7&&ang<=3.6)
            ang=3.14;
        else if(ang>3.6&&ang<=4.3)
            ang=3.95;
        else if(ang>4.3&&ang<5.0)
            ang=4.74;
        else if(ang>=5.0&&ang<5.8)
            ang=5.53;
        else if(ang>=5.8&&ang<=6.5)
            ang=6.28;
        else if(ang<=-0.5&&ang>=-1.3)
            ang=-0.79;
        else if(ang<-1.3&&ang>-2.0)
            ang=-1.57;
        else if(ang<=-2.0&&ang>-2.7)
            ang=-2.37;
        else if(ang<=-2.7&&ang>=-3.6)
            ang=-3.14;
        else if(ang<-3.6&&ang>=-4.3)
            ang=-3.95;
        else if(ang<-4.3&&ang>-5.0)
            ang=-4.74;
        else if(ang<=-5.0&&ang>-5.8)
            ang=-5.53;
        else if(ang<=-5.8&&ang>=-6.5)
            ang=-6.28;
    }
}
