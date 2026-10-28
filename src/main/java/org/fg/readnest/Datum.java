package org.fg.readnest; /**
  *
  * Klasse kapselt die Klasse Calendar und benötigte Operationen
  *
  * @version 2.1 vom 04.02.2016
  * @author Elisabeth Engel, Arne Friedrich
  */
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Datum {

  // Attribute
  private Calendar cal;
  // Ende Attribute
  
  //Konstruktoren
  public Datum (){
    cal = Calendar.getInstance();
  }
  
  public Datum (String datum){
    this();
    set(datum);
  }  
  
  // Anfang Methoden
  public void set(int tag, int monat, int jahr){
    cal.set(jahr, monat-1, tag);
  }
  
  // Format des Strings: "dd.MM.yyyy"
  public void set(String datum){    
    SimpleDateFormat meinDatumFormat = new SimpleDateFormat("dd.MM.yyyy");
    try{
      cal.setTime(meinDatumFormat.parse(datum));
    }
    catch(ParseException ie){
      
    }
  }
  
  public int getJahr(){
    return cal.get(Calendar.YEAR);
  }
  
  public int getMonat(){
    return cal.get(Calendar.MONTH)+1;
  }
  
  public int getTag(){
    return cal.get(Calendar.DAY_OF_MONTH);
  }
  
  public long getTimeInMillis(){
    return cal.getTimeInMillis();
  }
  
  // Ausgabeformat: "dd.MM.yyyy"
  public String toString(){
    SimpleDateFormat meinDatumFormat = new SimpleDateFormat("dd.MM.yyyy"); 
    return meinDatumFormat.format(cal.getTime());
  }
  
  public int berechneTage(Datum spaeter){
    long time = spaeter.getTimeInMillis() - cal.getTimeInMillis();  // Differenz in ms
    long days = Math.round( (double)time / (24. * 60.*60.*1000.) ); // Differenz in Tagen
    return (int)days;
  }
  
  public void setAktuellesDatum(){
    Calendar now = Calendar.getInstance();
    set(now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH)+1, now.get(Calendar.YEAR));
  }
  
  // Ende Methoden
}
