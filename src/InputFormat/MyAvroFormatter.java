package InputFormat;
import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import oracle.kv.KVStore;
import oracle.kv.KeyValueVersion;
import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;
import oracle.kv.hadoop.AvroFormatter;


public class MyAvroFormatter implements AvroFormatter 
{
	@Override
	public IndexedRecord toAvroRecord(KeyValueVersion record, KVStore store) 
	{
        AvroCatalog catalog = store.getAvroCatalog(); 
        String s = " { \"type\" : \"record\",\"name\" : \"nodeRecord\", \"fields\" : ["
         	     + "{ \"name\" :\"data\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"rules\", \"type\" :\"bytes\", \"default\" :\"null\" },"
                  + "{ \"name\" :\"parent\", \"type\" :\"string\", \"default\" :\"null\" },"
  	               + "{ \"name\" :\"duplicate\", \"type\" :\"string\", \"default\" :\"\" }"
         		 + "]}";
        Schema.Parser parser = new Schema.Parser();
        parser.parse(s);
        Schema schema = parser.getTypes().get("nodeRecord"); 
        GenericAvroBinding binding = catalog.getGenericBinding(schema);
        return binding.toObject(record.getValue());
	}

}
