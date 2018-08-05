package v1.enterurl

import java.util.{Date, UUID}

import javax.inject.{Inject, Provider}
import utils.{ApplicationResult, Config, Money}

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to and creation of the backend data
  */
class EnterUrlResourceHandler @Inject()(routerProvider: Provider[EnterUrlRouter],
                                        config: Config)(implicit ec: ExecutionContext) {
}
