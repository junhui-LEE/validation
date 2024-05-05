package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.*;

public class MessageCodesResolverTest {
    
    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
    /*
    * MessageCodesResolver 이것이 인터페이스 인데 열어서 확인해 보면 에러코드를 넣으면 
    * 여러개의 값들을 반환해 주는 것을 확인할 수 있다.
    * */
    
    @Test
    void messageCodesResolverObject(){
//        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
//        for (String messageCode : messageCodes) {
//            System.out.println("messageCode = " + messageCode);
//        }
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        assertThat(messageCodes).containsExactly("required.item", "required");
    }
    
    @Test
    void messageCodesResolverField(){
    //    String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
    //    for (String messageCode : messageCodes) {
    //        System.out.println("messageCode = " + messageCode);
    //    }
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required");
    }
    
}
