/*
import java.util.{Properties, StringTokenizer}

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.endpoint.StreamingEndpoint
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._


object TwitterStreaming {

  def main(args: Array[String]): Unit = {


    // Paramètres de connexion à l'API TWitter
    val props = new Properties()
    props.setProperty(TwitterSource.CONSUMER_KEY, "Yyk31XvynE8PRUimckJ8PuB7L")
    props.setProperty(TwitterSource.CONSUMER_SECRET, "QuDBhi9uvu670uOTZHglZUGxF897w5muge4Nk4bqIr39ueRXVo")
    props.setProperty(TwitterSource.TOKEN, "2578559808-CiCIuq1hSWHLhvKgDDnHFHG3IYlMTaOryn64S8B")
    props.setProperty(TwitterSource.TOKEN_SECRET, "B9dvKo30gq1wCJJsomrxqyxa6nOkAxOvvLPQeVxcRIyIt")


    // Création d'un endpoint personnalisé
    class FilterEndpoint extends TwitterSource.EndpointInitializer with Serializable {

       override def createEndpoint(): StreamingEndpoint = {
            val endpoint = new StatusesFilterEndpoint()
            endpoint.trackTerms(List("Trump").asJava)
            return endpoint
      }
    }
    //@TODO: gestion des erreurs en cas d'absence de Tweets
    class SelectedTweets extends FlatMapFunction[String, (String, Int)] {
      lazy val jsonParser = new ObjectMapper()

      override def flatMap(value: String, out: Collector[String]): Unit = {
        // deserialize JSON from twitter source
        val jsonNode = jsonParser.readValue(value, classOf[JsonNode])
        val isFrench = jsonNode.has("user") &&
          jsonNode.get("user").has("lang") &&
          jsonNode.get("user").get("lang").asText == "fr"
        val hasText = jsonNode.has("text")

        (isFrench, hasText, jsonNode) match {
          case (true, true, node) => {

            println("TEXT => ", jsonNode.get("text").textValue())
            println("USERNAME => ", jsonNode.get("user").get("name")textValue())
            println("USERLOCATION => ", jsonNode.get("user").get("location")textValue())
            println("COORDINATES => ", jsonNode.get("coordinates").textValue())

            /*
            val tweet = jsonNode.get("text").textValue()
            val user =  jsonNode.get("user").get("name")textValue()
            val location = jsonNode.get("user").get("location")textValue()
            */

            //@TODO:réussir le collect
           // out.collect(tweet, user, location)
          }
          case _ =>
        }
      }
    }

    val source = new TwitterSource(props)
    val customFilterInitializer = new FilterEndpoint()
    source.setCustomEndpointInitializer(customFilterInitializer)


    // Initialisation de l'environnement et de la source de données
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamSource: DataStream[String] = env.addSource(source)


    //Récupération et manipulation des données
    val tweets: DataStream[(String, Int)] = streamSource
      // selecting English tweets and splitting to (word, 1)
      .flatMap(new SelectedTweets)
      // group by words and sum their occurrences

    // emit result
      tweets.print()

    // execute program
    env.execute("Twitter Streaming Example")
  }



}
*/