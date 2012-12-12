package Experiments;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

public class AvroTest {

	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		//config
		KVStore store;
		KVStoreConfig config = new KVStoreConfig("kvstore", "localhost:5000");
        store = KVStoreFactory.getStore(config);
        AvroCatalog catalog = store.getAvroCatalog();
        File f = new File("/home/alex/Downloads/kv-2.0.15/schema2.avsc");
        Schema.Parser parser = new Schema.Parser();
        parser.parse(f);
        Schema schema = parser.getTypes().get("test2"); 
        GenericAvroBinding binding = catalog.getGenericBinding(schema);
        
        //serialize
        Object[] alphabet = {'a','b','c','d'};
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bo);
        out.writeObject(alphabet);
        
        //write
        GenericData.Record record = new GenericData.Record(schema);
        ByteBuffer a = ByteBuffer.wrap(bo.toByteArray());
        record.put("bla", a);
        Value value = binding.toValue(record);
        store.put(Key.createKey("bla"), value );
        
        //read
        ValueVersion myInput = store.get(Key.createKey("bla"));
        GenericRecord inputRecord = new GenericData.Record(schema);
        inputRecord  = binding.toObject(myInput.getValue());
        ByteBuffer temp = (ByteBuffer) inputRecord.get(0);
        byte[] buff = temp.array();
        
        //deserialize
		ByteArrayInputStream bi2 = new ByteArrayInputStream(buff);
        ObjectInputStream in2 = new ObjectInputStream(bi2);
        Object[] aphabet = (Object[]) in2.readObject();
        
        //print
        for(int i=0;i<aphabet.length;i++)
        {
        	 System.out.print(aphabet[i]);
        }
        System.out.println();
	}

}
