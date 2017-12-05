import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Article {

    public static final int VAL_SCORE = 457;
    public static final int WEEK = 604800;


    public static String ajoutArticle(Jedis conn, String utilisateur, String titre, String url) {
        String articleId = String.valueOf(conn.incr("article:"));
        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        HashMap<String,String> donnees = new HashMap<String, String>();
        donnees.put("titre", titre);
        donnees.put("lien", url);
        donnees.put("utilisateur", utilisateur);
        donnees.put("timestamp", String.valueOf(now));
        donnees.put("nbvotes", "1");
        conn.hmset(article, donnees);
        conn.zadd("date:article", now, article);
        initScores(conn,utilisateur,articleId,now);
        return articleId;
    }

    public static void initScores(Jedis conn, String utilisateur, String articleId, long date){
        String article = "articleVote:" + articleId;
        conn.sadd(article, utilisateur);
        conn.zadd("score:articleVote", date + Article.VAL_SCORE, articleId);
        conn.expire(article, Article.WEEK);
    }

    public static void voter(Jedis conn, String utilisateur, String articleId) {
        if (!conn.smembers("articleVote:" + articleId).contains(utilisateur)) {
            conn.sadd("articleVote:" + articleId, utilisateur);
            conn.hincrBy("article:"+articleId, "nbvotes", 1);
            conn.zincrby("score:article", Article.VAL_SCORE, articleId);
            conn.zincrby("nbvotes:", 1, "article:"+articleId);
        }
    }

    public static Set<String> getAll(Jedis conn){
        return conn.zrange("date:article",0,-1);
    }

    public static Map<String,String> findOne(Jedis conn, String key){
        return conn.hgetAll(key);
    }

    public static Double getScoreByArticle(Jedis conn, String articleId) {
        return conn.zscore("score:articleVote", articleId);
    }

    public static Set<String> get10BestArticles(Jedis conn) {
        return conn.zrevrange("nbvotes:", 0, 9);
    }

}
