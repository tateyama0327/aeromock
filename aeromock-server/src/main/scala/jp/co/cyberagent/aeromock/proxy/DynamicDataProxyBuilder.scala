package jp.co.cyberagent.aeromock.proxy

import jp.co.cyberagent.aeromock.core.javassist.DynamicProxyBuilder
import scala.reflect.ClassTag
import javassist.CtClass
import jp.co.cyberagent.aeromock.data.MethodDef
import javassist.CtMethod
import jp.co.cyberagent.aeromock.helper._
import jp.co.cyberagent.aeromock.data.ReturnValueStore
import javassist.Modifier
import jp.co.cyberagent.aeromock.data.InstanceProjection

/**
 * Builder to create dynamic proxy to template.
 * @author stormcat24
 */
abstract class DynamicDataProxyBuilder[C: ClassTag](projection: InstanceProjection) extends DynamicProxyBuilder[C] {

  val javaMapMethods = classOf[java.util.Map[_, _]].getMethods().map(_.getName())

  protected def createMethod(proxy: CtClass, method: MethodDef): CtMethod = {
    val content = s"""
      public Object ${method.name}(Object[] args) {
          String key = "${method.name}";
          return ${getObjectFqdn(ReturnValueStore)}.fetch(this.getClass().getName(), key);
      }
    """

    method.value match {
      case instance: InstanceProjection => {
        val delegateInstance = instance.toInstanceJava
        val ctMethod = CtMethod.make(content, proxy)
        ReturnValueStore.put(proxy.getName(), method.name, delegateInstance)
        ctMethod.setModifiers(ctMethod.getModifiers() | Modifier.VARARGS)
        ctMethod
      }
      case v => {
        val ctMethod = CtMethod.make(content, proxy)
        val filtered = projection.decoration(projection.processPropertyJava(v))
        ReturnValueStore.put(proxy.getName(), method.name, filtered)
        ctMethod.setModifiers(ctMethod.getModifiers() | Modifier.VARARGS)
        ctMethod
      }
    }
  }

}
