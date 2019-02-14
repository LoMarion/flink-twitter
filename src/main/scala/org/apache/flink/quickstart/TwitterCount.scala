
import java.util.{Properties, StringTokenizer}

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.endpoint.StreamingEndpoint
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._


object TwitterCount {

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

    val source = new TwitterSource(props)
    val customFilterInitializer = new FilterEndpoint()
    source.setCustomEndpointInitializer(customFilterInitializer)


    // Initialisation de l'environnement et de la source de données
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamSource: DataStream[String] = env.addSource(source)


    //Récupération et manipulation des données

    val tweets = streamSource
      .map(s => (0, 1))
      .keyBy(0)
      .timeWindow(Time.minutes(2), Time.seconds(30))
      .sum(1)
      .map(t => t._2)


    // emit result
    tweets.print()

    // execute program
    env.execute("Twitter Streaming Example")
  }



}