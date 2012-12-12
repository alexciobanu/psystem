package Experiments;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Serial 
{
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException 
	{
		int[][] data = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}};
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bo);
        out.writeObject(data);

        byte[] buff = bo.toByteArray();

		ByteArrayInputStream bi = new ByteArrayInputStream(buff);
        ObjectInputStream in = new ObjectInputStream(bi);
        int[][] array = (int[][]) in.readObject();


        //System.out.println("It is " + (array instanceof Serializable) + " that int[] implements Serializable");
        //System.out.print("Deserialized array: " + array[0]);
        for (int i=0; i<3; i++) 
        {
        	for(int j=0;j<3;j++)
        	{
            System.out.print(" " + array[i][j]);
        	}
        	System.out.println();
        }   
	}

}
