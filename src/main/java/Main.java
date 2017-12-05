import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;

public class Main {

    public static final String host = "localhost";
    public static final int port = 6379;
    public static final int redisDB = 2;

    @BeforeClass
    public static void cleanUp() {
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        // Si la base existe
        if(redisDB != 0){
            jedis.flushDB();
        }
    }

    public static final void main(String[] args){

        Jedis conn = new Jedis("localhost");
        conn.set("cle","valeur");
        String val = conn.get("cle");
        System.out.println(val);

        Article.ajoutArticle(conn, "Jhon","Redis for dummies", "http://foo/bar/1");
        System.out.println(Article.getAll(conn).size());

        System.out.println(Article.findOne(conn,"article:1").toString());

    }

}
