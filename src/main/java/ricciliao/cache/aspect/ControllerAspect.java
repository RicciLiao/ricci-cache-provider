package ricciliao.cache.aspect;

import org.aopalliance.intercept.MethodInvocation;
import ricciliao.common.component.exception.CmnParameterException;
import ricciliao.common.component.response.ResponseUtils;
import ricciliao.dynamic.aop.DynamicAspect;

public class ControllerAspect extends DynamicAspect {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {

            return super.invoke(invocation);
        } catch (CmnParameterException e) {

            return ResponseUtils.builder().code(e.getCode()).build();
        } catch (Exception e) {

            return ResponseUtils.errorResponse();
        }
    }

}
