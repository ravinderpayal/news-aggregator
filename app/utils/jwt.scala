package utils

import java.util.{Date, HashMap, UUID}

import models.UserLoggedIn
import pdi.jwt.JwtSession


object jwt{

  def generate(issuer:String,
               userLoggedIn: UserLoggedIn) = {
    JwtSession() + ("user", userLoggedIn) + ("issuer", issuer)
  }
  def renew(session:JwtSession): String = {
    session.refresh().serialize
  }
}
