import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.util.Collections;
import java.util.Properties;

public class Consumatore {
    private String topic;
    private String bootstrapServers; //Lista di indirizzoIP:porta separati da una virgola.
    private String nome;
    private Consumer<Long, String> consumatore;

    //definisco la classe consumatore e ne istanzio uno
    Consumatore(String topic, String nomeConsumatore, String boostrapServers) {
        this.topic = topic;
        this.bootstrapServers = boostrapServers;
        this.nome = nomeConsumatore;

        //Imposto le proprietà del consumatore da creare
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, nomeConsumatore);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        Consumer<Long, String> cons = new KafkaConsumer<>(props);

        //Sottoscrivo il consumatore al topic
        cons.subscribe(Collections.singletonList(topic));

        this.consumatore = cons;
    }

    //mi collego a kafka e prendo i records del consumatore
    static String rispostaConsumatore(Consumatore consumatore) throws InterruptedException {
        System.out.println("Consumatore "+consumatore.nome+" richiesta dati avviata");

        final ConsumerRecords<Long, String> recordConsumatore = consumatore.consumatore.poll(1000);
        if (recordConsumatore.count()==0) {
            System.out.println("Nessun record trovato");
        }
        String datiJson=null;

        for(ConsumerRecord<Long, String> record : recordConsumatore) {
            //produco il file JSON
            datiJson+="{";
            datiJson+="nome: "+consumatore.nome+"; Chiave: "+record.key()+"; Valore: "+record.value()+"; Partizione: "
                    +record.partition()+"; Offeset: "+record.offset()+"; }";
        };

//        recordConsumatore.forEach((record) -> {
//            datiJson+="{";
//            datiJson+="nome: "+consumatore.nome+"; Chiave: "+record.key()+"; Valore: "+record.value()+"; Partizione: "
//                +record.partition()+"; Offeset: "+record.offset()+"; }";
//
//        });
//        consumatore.consumatore.commitAsync();
        System.out.println("Messaggi disponibili consumati!");
        return datiJson;


    }

    public static void main(String args[]) throws Exception {

        Consumatore test = new Consumatore("TopicDiProva","consumatoreTest", "localhost:29092");

        Consumatore.rispostaConsumatore(test);
    }
}
