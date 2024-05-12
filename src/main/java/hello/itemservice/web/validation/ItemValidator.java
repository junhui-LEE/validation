package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz){
        return Item.class.isAssignableFrom(clazz);
        /*
        boolean supports(Class<?> clazz); => 검증기를 지원하느냐? 라는 의미이다.
        지금은 item을 검증하는 검증기 이니까 return Item.class.isAssignableFrom(clazz);
        이렇게 만들었다.

        isAssignableFrom()은 인자로 들어간 clazz가 Item클래스이거나 Item클래스의 자식클래스 이면
        true를 반환한다. 그렇지 않으면 false를 반환한다.
        */
    }

    @Override
    public void validate(Object target, Errors errors){
        /*
        void validate(Object target, Errors errors); => 실제로 검증하는 메서드이다.
        인자로 검증 대상 객체와 bindingResult가 들어간다. 여기서는 item과 bindingResult가 들어간다.

         */
        Item item = (Item)target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if(item.getQuantity() == null || item.getQuantity() > 10000){
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드 예외가 아닌 전체 예외
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000){
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

    }
}
