package scrapper

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

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
  // from here we will be getting links to be shown in table
  def get = {
    dataStore.get.map(a => a)
  }

  def annotate(imgId: Int, annotation: String) = {
        dataStore.addAnnotation(imgId, annotation)
  }

  def blankImage(imgId: Int) = dataStore.removeImage(imgId)

  def countImageAnnotations = dataStore.countImageAnnotations

    // Here we are returning a Future wrapper and not defining the logic, as I am busy, we will do this later
}