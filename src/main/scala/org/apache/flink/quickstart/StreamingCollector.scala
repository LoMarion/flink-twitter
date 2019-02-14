import java.util.Properties

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.endpoint.StreamingEndpoint
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector
import org.apache.flink.api.common.functions.FlatMapFunction

import scala.collection.JavaConverters._
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.flink.streaming.api.windowing.time.Time


object StreamingCollector {

  def main(args: Array[String]): Unit = {


    // Paramètres de connexion à l'API TWitter
    val props = new Properties()
    props.setProperty(TwitterSource.CONSUMER_KEY, "Yyk31XvynE8PRUimckJ8PuB7L")
    props.setProperty(TwitterSource.CONSUMER_SECRET, "QuDBhi9uvu670uOTZHglZUGxF897w5muge4Nk4bqIr39ueRXVo")
    props.setProperty(TwitterSource.TOKEN, "2578559808-CiCIuq1hSWHLhvKgDDnHFHG3IYlMTaOryn64S8B")
    props.setProperty(TwitterSource.TOKEN_SECRET, "B9dvKo30gq1wCJJsomrxqyxa6nOkAxOvvLPQeVxcRIyIt")


    val source = new TwitterSource(props)
    val customFilterInitializer = new FilterEndpoint()
    source.setCustomEndpointInitializer(customFilterInitializer)


    // Initialisation de l'environnement et de la source de données
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamSource: DataStream[String] = env.addSource(source)


    //Récupération et manipulation des données
    val tweets = streamSource
      // selecting English tweets and splitting to (word, 1)
      .flatMap(new ExtractHashTags)
        .keyBy(0)
        .timeWindow(Time.seconds(30))
        .sum(4)
    // emit result
      tweets.print()

    // execute program
    env.execute("Twitter Streaming Example")
  }

  // Création d'un endpoint personnalisé
  class FilterEndpoint extends TwitterSource.EndpointInitializer with Serializable {

    override def createEndpoint(): StreamingEndpoint = {
      val endpoint = new StatusesFilterEndpoint()
      endpoint.trackTerms(List("Trump").asJava)
      return endpoint
    }
  }

  private class ExtractHashTags extends FlatMapFunction[String, (String, String, String, String, Int)] {
    lazy val jsonParser = new ObjectMapper()

    override def flatMap(tweetJsonStr: String, out: Collector[(String, String, String, String, Int)]): Unit = {
      //Désérialisation du jsonNode
      val tweetJson = jsonParser.readValue(tweetJsonStr, classOf[JsonNode])

      val isEnglish = tweetJson.has("user") &&
        tweetJson.get("user").has("lang") &&
        tweetJson.get("user").get("lang").asText == "en"

      val hasText = tweetJson.has("text")
      val hasHashTags = tweetJson.has("entities") &&
        tweetJson.get("entities").has("hashtags")

      (isEnglish, hasText, hasHashTags) match {
        case (true, true, true) => {
          val hashtags = tweetJson.get("entities").get("hashtags")
          val hashtagsSize = hashtags.size()

          val username = tweetJson.get("user").get("name").textValue()
          val location = tweetJson.get("user").get("location").textValue()
          val text = tweetJson.get("text").textValue()

          if (hashtagsSize != 0) {
            val myIterator = hashtags.elements()

            while (myIterator.hasNext) {
              val slaidNote = myIterator.next()
              val hashtag = slaidNote.get("text").textValue()
              //println(username, location, hashtag, text)
              out.collect((username, location, hashtag, text, 1))
            }
          }
        }
          case _ =>
        }
      }
    }
}
