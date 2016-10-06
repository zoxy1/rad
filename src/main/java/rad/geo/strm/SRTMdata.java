package rad.geo.strm;

/*======================================================================================*/
/*==========================                                     =======================*/
/*======================                                              ==================*/
/*==================         Warning! this Class is under                 ==============*/
/*======================          construction!!!                     ==================*/
/*==========================                                     =======================*/
/*======================================================================================*/


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 18.03.2006
 * Time: 14:40:33
 * To change this template use File | Settings | File Templates.
 */
public class SRTMdata {
    private boolean isDataLoad;
    private int rows;
    private int cols;
    private Byte[] byteBuffer;
    private short[][] data;
    private double resolution = 800.0;

//``````````````````
    SRTMdata(){
    }

    ////////////////////////////open ".dem" file ad read data/////////////////////////////
    void convertToShort(int dimension){
        assert isDataLoad;
        int i=0; // rows cycle
        int j=0; // cols cycle
        int k=0; // cols count
        Byte temp1;
        Byte temp2;
        Integer temp3;
        if (dimension > 0) {
            for(i=0; i<rows; i++){ //rows
                k = 0;
                for(j=0; j<cols*2; j++){ //gather column's elements from 2 differrent bytes
                    temp1 = byteBuffer[i*j];
                    temp2 = byteBuffer[i*j+1];
                    temp3 =temp1.shortValue() << 8;
                    data[i][j] = temp3.shortValue();// + temp2.shortValue();
                    k++;
                }
            }
        } //end "if"
    } //end of conversion method


    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }


} //end of class definition
