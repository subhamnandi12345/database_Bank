// src/main/scala/controllers/BankController.scala
package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import models.{Customer, MySQLBankManagementSystem}
import models.BankManagementSystem
import play.api.i18n.Lang.jsonTagWrites
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsObject.writes
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites


@Singleton
class BankController @Inject()(cc: ControllerComponents,bnk : MySQLBankManagementSystem) extends AbstractController(cc) {

  implicit val customerFormat: Format[Customer] = Json.format[Customer]
  implicit val customerWrites: Writes[Customer] = Json.writes[Customer]
  implicit val customerReads: Reads[Customer] = Json.reads[Customer]

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())

  }

  private def withCustomer(id: Int)(operation: Customer => Result): Result = {
    bnk.showCustomer(id) match {
      case Some(customer) => operation(customer)
      case None => NotFound(s"Customer with ID $id not found.")
    }
  }

  def addCustomerOne = Action(parse.json) { request =>
    val customerData = request.body.as[Customer]
    bnk.addCustomer(customerData.name, customerData.id, customerData.balance)
    Ok(Json.toJson(customerData))
  }

  def deleteCustomerOne(id: Int) = Action {
    bnk.deleteCustomer(id)
    Ok(s"Customer with ID $id deleted.")
  }

  def showCustomerOne(id: Int) = Action {
    val res = bnk.showCustomer(id) map { idd =>
      Json.toJson(idd)
    }
    Ok(res.get)
  }

  /*def showCustomer(id: Int) = Action {
    bnk.showCustomer(id) map {
      case Some(customer) => Ok(Json.toJson(customer))
      case None => NotFound(s"Customer with ID $id not found.")
    }
  }*/
  def transferMoney = Action(parse.json) { request =>
    val transferData = request.body.as[JsObject]
    val fromId = (transferData \ "fromId").as[Int]
    val toId = (transferData \ "toId").as[Int]
    val amount = (transferData \ "amount").as[Double]
    bnk.transferMoney(fromId, toId, amount)
    Ok(s"$amount transferred from customer $fromId to customer $toId.")
  }

//def updateCustomerone(id: Int): Action[JsValue] = Action(parse.json) { request =>
//  val updateData = request.body.as[JsObject]
//  val updatedBalance = (updateData \ "balance").as[Double]
//
//  // Higher-order function to update the customer's balance
//  val updateOperation: CustomerOperation = customer => {
//    bnk.updateCustomer(id, updatedBalance)
//    Ok(s"Customer with ID $id updated. New balance: $updatedBalance")
//  }
//
//  withCustomer(id)(updateOperation)
//}

}
