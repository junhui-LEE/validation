package hello.itemservice.web.validation;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    @PostMapping("/add")
    public Object addItem(@RequestBody @Validated ItemSaveForm form,
                          BindingResult bindingResult
                          ){
        /*
        * 이번에는 api로 받을것이다. 즉, json형식으로 받고 싶은 것이다. 그런데 검증하는 기능도 넣고 싶은 것이다.
        * */
        log.info("API 컨트롤러 호출");

        if(bindingResult.hasErrors()){
            log.info("검증 오류 발생 errors={}", bindingResult);
            return bindingResult.getAllErrors();
            /*
            * bindingResult가 가지고 있는 모든 오류, ObjectError와 그 자식인 FieldError 전부 반환해 준다.
            * 리스트로 반환되면 json으로 변환해서 화면에 보여줄 것이다.
            * */
        }

        log.info("성공 로직 수행");
        return form;
    }

}
