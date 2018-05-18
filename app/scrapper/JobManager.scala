package scrapper

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

@Singleton
class JobManager @Inject()(dataStore: DataStore, scrapper: Scrapper)(implicit executionContext: ExecutionContext) {
  def scrap(pageLink: String): Unit = {
    println("inserted")
    val images = scrapper.scrap(pageLink)
    println("inserted")
    images.foreach(a => dataStore.insert(pageLink,a._1, a._2))
  }
  def get(link: String) = {
    dataStore.get(link).map(a => a)
  }

}