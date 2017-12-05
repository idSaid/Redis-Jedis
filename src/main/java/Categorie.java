import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Categorie {

    public static void addArticle(Jedis conn, String articleId, String category) {
        conn.sadd("category:" + category, articleId);
    }

    public static void removeArticle(Jedis conn, String articleId, String categorie) {
        conn.srem("category:" + categorie, articleId);
    }

    public static Set<String> getAll(Jedis jedis, String categorie){
        return jedis.smembers("category:"+categorie);
    }

    public static HashMap<String, Double> getScores(Jedis conn, String categorie) {
        Set<String> articles = conn.smembers("category:"+categorie);
        HashMap<String,Double> scores = new HashMap<String, Double>();
        for (String articleId: articles) {
            String article = Article.findOne(conn, "article:" + articleId).get("titre");
            scores.put(article, Article.getScoreByArticle(conn, articleId));
        }
        return scores;
    }

}
