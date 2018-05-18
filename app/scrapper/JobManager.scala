package scrapper

import javax.inject.{Inject, Singleton}

@Singleton
class JobManager @Inject()(dataStore: DataStore, scrapper: Scrapper) {
  def scrap(pageLink: String): Unit = {
    val images = scrapper.scrap(pageLink)
    images.foreach(a => dataStore.insert(pageLink,a._1, a._2))
  }
}