import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class Tests {

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

    @Test
    public void testConnexion(){
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        assertEquals(jedis.ping(), "PONG");
    }

    @Test
    public void testCleValeur(){
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        jedis.set("macle", "toto");
        assertEquals(jedis.get("macle"), "toto");
    }

    @Test
    public void testAjoutArticle(){
        cleanUp();
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        Article.ajoutArticle(jedis,"Jhon","Redis for dummies", "http://foo/bar/1");
        assertEquals("Redis for dummies",Article.findOne(jedis,"article:1").get("titre"));
        assertEquals("Jhon",Article.findOne(jedis,"article:1").get("utilisateur"));
        assertEquals("http://foo/bar/1",Article.findOne(jedis,"article:1").get("lien"));
    }

    @Test
    public void testVoterAndGet10BestArticles(){
        cleanUp();
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        for (int i=0; i<15; i++){
            String articleId = Article.ajoutArticle(jedis,"Jhon","Redis for dummies"+i, "http://foo/bar/1");
        }
        for (int i=0; i<10; i++){
            Article.voter(jedis, "doe", "article:"+i);
            Article.voter(jedis, "mike", "article:"+(i+1));
            Article.voter(jedis, "any", "article:"+(i+2));
        }
        Set<String> bestArticles = Article.get10BestArticles(jedis);
        System.out.println("10 best articles :\n"+bestArticles.toString());
        assertEquals(10, bestArticles.size());
    }

    @Test
    public void testAjoutCategorie() {
        cleanUp();
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        String categorie = "DBMS";
        String article = Article.ajoutArticle(jedis,"Jhon","Redis for dummies", "http://foo/bar/1");
        Categorie.addArticle(jedis, article, categorie);
        assertEquals(false,Categorie.getAll(jedis, categorie).isEmpty());
    }


    @Test
    public void testSuppressionCategorie() {
        cleanUp();
        Jedis jedis = new Jedis(host, port);
        jedis.select(redisDB);
        String categorie = "DBMS";
        String article = Article.ajoutArticle(jedis,"Jhon","Redis for dummies", "http://foo/bar/1");
        Categorie.addArticle(jedis, article, categorie);
        assertEquals(false,Categorie.getAll(jedis, categorie).isEmpty());
        Categorie.removeArticle(jedis, article, categorie);
        assertEquals(true,Categorie.getAll(jedis, categorie).isEmpty());
    }

    @Test
    public void getArticleWithScore() {
        Jedis conn = new Jedis(host, port);
        conn.select(redisDB);

        for (int i=1;i<5;i++){
            String article = Article.ajoutArticle(conn,"Jhon","Redis for dummies"+i, "http://foo/bar/1"+i);
            Article.voter(conn, "Doe", article);
            Categorie.addArticle(conn, article, "DBMS");
        }

        for (int i=1;i<5;i++){
            String article = Article.ajoutArticle(conn,"Jhon","Java for dummies"+i, "http://foo/bar/1"+i);
            Article.voter(conn, "Doe", article);
            Categorie.addArticle(conn, article, "Dev");
        }

        HashMap<String, Double> scoresDBMS = Categorie.getScores(conn, "DBMS");
        HashMap<String, Double> scoresDev = Categorie.getScores(conn, "Dev");

        System.out.println("\n Scores d'articles regroupés par catégories:");
        System.out.println(scoresDBMS.toString());
        System.out.println(scoresDev.toString());

        assertEquals(4, scoresDBMS.size());
        assertEquals(4, scoresDev.size());
    }

}
