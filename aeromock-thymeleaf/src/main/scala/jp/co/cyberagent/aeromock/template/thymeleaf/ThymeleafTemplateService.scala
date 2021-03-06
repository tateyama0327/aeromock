package jp.co.cyberagent.aeromock.template.thymeleaf

import java.io.{StringWriter, Writer}

import jp.co.cyberagent.aeromock.core.annotation.TemplateIdentifier
import jp.co.cyberagent.aeromock.core.http.ParsedRequest
import jp.co.cyberagent.aeromock.data.InstanceProjection
import jp.co.cyberagent.aeromock.helper._
import jp.co.cyberagent.aeromock.template.{TemplateAssertError, TemplateAssertFailure, TemplateAssertResult, TemplateService}
import jp.co.cyberagent.aeromock.{AeromockTemplateNotFoundException, AeromockTemplateParseException}
import org.thymeleaf.context.Context
import org.thymeleaf.exceptions.{TemplateEngineException, TemplateInputException}

import scala.language.dynamics
import scalaz.Scalaz._

/**
 * [[jp.co.cyberagent.aeromock.template.TemplateService]] for Thymeleaf.
 * @author stormcat24
 */
@TemplateIdentifier(name = "thymeleaf", configType = classOf[ThymeleafConfigDef])
class ThymeleafTemplateService(config: ThymeleafConfig) extends TemplateService {

  val engine = TemplateEngineFactory.create(project._template.root, project._function.root, config)

  /**
   * @inheritdoc
   */
  override def renderHtml(request: ParsedRequest, projection: InstanceProjection): String = {
    val context = new Context()

    val proxyMap = projection.toInstanceJava().asInstanceOf[java.util.Map[String, _]]
    context.setVariables(proxyMap)

    val writer = new StringWriter
    val templatePath = request.url
    try {
      engine.process(templatePath, context, writer)
    } catch {
      case e: TemplateEngineException => throw new AeromockTemplateParseException(templatePath, e)
      case e: TemplateInputException => throw new AeromockTemplateNotFoundException(templatePath, e)
    }
    writer.toString
  }

  /**
   * @inheritdoc
   */
  override def templateAssertProcess(templatePath: String): Either[TemplateAssertResult, (Any, Writer) => Unit] = {
    val startTimeMills = System.currentTimeMillis()
    try {
      Right((param: Any, writer: Writer) => {
        val context = new Context()
        context.setVariables(param.asInstanceOf[java.util.Map[String, _]])
        engine.process(templatePath, context, writer)
      })
    } catch {
      case e: TemplateEngineException => Left(TemplateAssertFailure(getDifferenceSecondsFromNow(startTimeMills), e.getMessage))
      case e: Exception => Left(TemplateAssertError(getDifferenceSecondsFromNow(startTimeMills), e.getMessage))
    }
  }

  /**
   * @inheritdoc
   */
  override def extension: String = config.suffix | ".html"
}
