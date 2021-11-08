package com.imooc.miaosha.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.imooc.miaosha.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果值必须的，要判断格式
        if(required) {
            return ValidatorUtil.isMobile(value);
        }else {
            // 如果不是必须的，且为空，判断格式
            if(StringUtils.isEmpty(value)) {
                return true;
            }else {
                // 如果不为空，判断格式
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
