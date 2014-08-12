package ameba.aeromock.template.freemarker.method

import ameba.aeromock.core.http.RequestManager
import ameba.aeromock.core.script.GroovyDirectiveScriptRunner
import freemarker.template.TemplateMethodModelEx
import groovy.lang.Binding

/**
 * Implementation of [[freemarker.template.TemplateMethodModelEx]] to define function in Groovy.
 * @author stormcat24
 */
class AeromockCustomMethod(runner: GroovyDirectiveScriptRunner, scriptName: String)
  extends TemplateMethodModelEx {

  /**
   * @inheritdoc
   */
  override def exec(arguments: java.util.List[_]): AnyRef = {
    val binding = new Binding
    binding.setVariable("arguments", arguments)
    RequestManager.getRequestMap().foreach(entry => binding.setVariable(entry._1, entry._2))
    binding.setVariable("_data", RequestManager.getDataMap())

    runner.run[AnyRef](scriptName, binding)
  }
}