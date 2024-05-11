package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        /*
        * item에 바인딩 된 결과가 BindingResult bindingResult에 담기는 것이다. 잘 담길 수도 있지만 잘 안담겨서 오류가 생길 수도 있다.
        * 그럴경우 에도 bindingResult에 담긴다. 일단 그정도로 이해하면 된다.
        *
        * BindingResult bindingResult가 저번에 만든 Map<String, String> errors = new HashMap<>(); 의 역할을 해준다.
        *
        * bindingResult <= 바인딩 된 결과가 담긴다.
        * */

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증 - 가격*수량 은 10000 이상 이라는 요구사항
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice < 10000){
//                bindingResult는 바인딩된 결과를 담는곳이고, 제대로 바인딩 되었던지, 바인딩 되는 과정에서 오류가 발생했던지
//                바인딩된 결과가 bindingResult에 담기게 된다. 그런데 필드 에러가 아니라 글로벌 에러도 bindingResult에
//                넣어주면 되는데 글로벌 오류는 특정 필드에 대한 오류가 아니라 그냥 글로벌 오류이다. 그때는 ObjectError을 통해서
//                에러를 생성한 다음에 bindingResult에 에러를 추가해 주면 된다.
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 보내는 로직
        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            /*
            * model.addAttribute("bindingResult", bindingResult); 이렇게 안해도 된다.
            * 왜냐하면 bindingResult는 자동으로 뷰에 같이 넘어간다. 그래서 생략 가능하다.
            * 당연하겠다. 스프링에 바인딩 오류 관련 매커니즘이 있다는 것은 화면에서 쓰겠다는
            * 것이기 때문에 굳이 모델에 안담아도 스프링이 그런 것 까지 전부 다 해준다.
            * */
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
//            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
            bindingResult.addError(new FieldError(
                    "item",
                    "itemName",
                    item.getItemName(),
                    false,
                    null,
                    null,
                    "상품 이름은 필수 입니다."));
            /*
            * new FieldError()의 두번째 생성자를 살펴보자
            *
            * 세번쨰 인자 : item.getItemName()을 써 줬는데, 데이터타입이 @Nullable Object rejectValue이다.
            *           거절된 값, 즉, 사용자가 잘못 입력한 값을 써주는 것이다.
            * 네번쨰 인자 : false을 써 줬는데, 이것은 boolean bindingFailure이다.바인딩에 실패 했는지를 설정하는 것이다.
            *           예를들면 item에 있는 private Integer price; 여기에 데이터 들어가는 것(넘어오는것)이 실패했는지
            *           설정하는 것이다. 타입오류(가격 : qqqq) 이렇게 입력하면 private Integer price;에 들어가지를 않는다.
            *           데이터는 (가격 : 10) 이렇게 잘 넘어갔기 때문에 false로 했다.
            * 다선번째 인자 & 여섯번째 인자 :
            *           다섯번째 인자는 @Nullable String[] codes를 넣어주는 곳인데 null을 써 줬다. 여섯번째 인자는
            *           @Nullable Object[] arguments를 넣어주는 곳인데 null을 써줬다. 이것들(codes, arguments)은
            *           메시지화 해서 defaultMessage를 대체할 수 있는데, 뒤에서 설명하겠다.
            * 일곱번째 인자: @Nullable String defaultMessage를 넣어주는 곳인데 "상품 이름은 필수 입니다."를 넣어 주었다.
            * */

        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
//            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
            bindingResult.addError(new FieldError(
                    "item",
                    "price", item.getPrice(),
                    false,
                    null,
                    null,
                    "가격은 1,000 ~ 1,000,000 까지 허용합니다." ));

        }
        if(item.getQuantity() == null || item.getQuantity() >= 9999){
//            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
            bindingResult.addError(new FieldError(
                    "item",
                    "quantity",
                    item.getQuantity(),
                    false,
                    null,
                    null,
                    "수량은 최대 9,999 까지 허용합니다."));
        }

        // 특정 필드가 아닌 복합 룰 검증 - 가격*수량 은 10000 이상 이라는 요구사항
        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice < 10000){
//                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
                bindingResult.addError(new ObjectError(
                        "item",
                        null,
                        null,
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
                /*
                * 특정 필드의 오류가 아니라 복합적인 룰에 어긋나서 에러를 bindingResult에 넣어야 하는 경우, 글로벌 오류일 경우
                * new ObjectError()을 bindingResult에 넣는데, ObjectError의 두번째 생성자를 살펴보자
                *
                * 두번째 인자 & 세번째 인자: @Nullable String[] codes와 @Nullable String[] arguments인데
                *        ObjectError은 필드로 데이터 넘어오는 것이 없다. 그래서 둘다 null을 써줬다. 이것들(codes, arguments)은
                *        메시지화 해서 defaultMessage를 대체할 수 있는데, 뒤에서 설명하겠다.
                * 네번째 인자: @Nullable String defaultMessage를 넣어주는 곳인데 "가격 * 수량의 합은 10,000원 이상이어야 합니다.
                *        현재 값 = "+resultPrice를 넣어 주었다.
                * */
            }
        }

        // 검증에 실패하면 다시 입력 폼으로 보내는 로직
        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        // 검증 로직
        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError(
                    "item",
                    "itemName",
                    item.getItemName(),
                    false,
                    new String[]{"required.item.itemName"},
                    null,
                    "상품 이름은 필수 입니다."));
            /*
            * new String[]{"required.item.itemName"} 이렇게 써줌으로서 스프링의 메시지소스가 errors의
            * required.item.itemName을 찾아서 그 값을 오류메시지로 사용한다. 만일 errors.properties에
            * required.item.itemName가 없으면 사용자(개발자)가 문자열배열의 두번째로 설정한 것을 찾아서
            * 그 값을 오류메시지로 사용한다. 이를테면 new String[]{"required.item.itemName, "two"}로
            * 개발자가 넣어 놓는다면 그리고 errors.properties에 required.item.itemName가 없다면
            * 두번째 값은 two을 찾아서 오류메시지로 사용한다. 아무것도 errors.properties에 없다면 스프링은
            * defaultMessage를 사용해서 오류메시지를 출력하고 defaultMessage도 null이면 오류페이지 화면이
            * 나온다.
            * */
        }
        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError(
                    "item",
                    "price", item.getPrice(),
                    false,
                    new String[]{"range.item.price"},
                    new Object[]{1000, 1000000},
                    "가격은 1,000 ~ 1,000,000 까지 허용합니다." ));

        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError(
                    "item",
                    "quantity",
                    item.getQuantity(),
                    false,
                    new String[]{"max.item.quantity"},
                    new Object[]{9999},
                    "수량은 최대 9,999 까지 허용합니다."));
        }

        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice < 10000){
                bindingResult.addError(new ObjectError(
                        "item",
                        new String[]{"totalPriceMin"},
                        new Object[]{10000, resultPrice},
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
            }
        }

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());
//        - 오류 코드와 메시지 처리6 -
//      스프링이 만들어준 에러코드를 MessageCodesResolver에 넣음으로서 만들어진 메시지 코드에
//      대한 값을 errors.properties에 추가해 줌으로서 price에 대한 타입오류가 발생할 시에
//      나오는 에러 메시지를 우리가 설정한 "숫자를 입력해주세요."로 나오게끔 개발 하였다. 하지만
//      사용자가 A를 price에 입력할 시에 검증로직에 있는 item.getPrice() == null에 걸리기 때문에
//      price를 검증하는 검증로직에서 발생하는 에러 메시지("가격은 1,000 ~ 1,000,000 까지 허용합니다.") 또한
//      출력되는 것을 확인할 수 있다. 내가 만일 둘줄 하나의 에러 메시지만을 보여주게끔 개발하고 싶다면 그 중에서
//      "숫자를 입력해주세요"만을 보여주게끔 처리하고 싶다면 아래와 같은 코드를 추가해 주면 된다.
//      이런식으로 개발이 가능하다.
//        if(bindingResult.hasErrors()){
//            log.info("errors = {}", bindingResult);
//            return "validation/v2/addForm";
//        }

        /*
        * 컨트롤러에서 BindingResult는 검증해야 할 객체인 target바로 다음에 온다. 여기에는 어떤의미가 내포되어 있냐면
        * BindingResult는 이미 본인이 검증해야할 객체인 target을 알고 있다. FieldError에서 바인딩 되는 객체 이름(item)을
        * 사실은 넣어줄 필요 없다. 생략할 수 있다. 왜냐하면 bindingResult는 target을 이미 알고 있기 때문이다.
        *
        * BindingResult가 제공하는 rejectValue(), reject()를 사용하면 FieldError, ObjectError를 직접 생성하지 않고
        * 깔끔하게 검증오류를 다룰 수 있다.
        * */
        // 검증 로직
//        if(!StringUtils.hasText(item.getItemName())){
////            bindingResult.addError(new FieldError(
////                    "item",
////                    "itemName",
////                    item.getItemName(),
////                    false,
////                    new String[]{"required.item.itemName"},
////                    null,
////                    "상품 이름은 필수 입니다."));
//            bindingResult.rejectValue("itemName", "required");
//            /*
//            * rejectValue의 첫번째 파라미터 : 필드이름을 써준다.
//            * rejectValue의 두번째 파라미터 : 에러코드를 써준다.
//            *
//            * bindingResult.rejectValue("itemName", "required");이렇게만 써줘도 bindingResult는 item을 이미 알고 있고
//            * itemName도 rejectValue()에 써줬기 때문에 알고 있다. 따라서 required만 써줘도 required.item.itemName을 찾는다.
//            * => 규칙이 있다. => 에러코드.오브젝트명.필드명
//            * */
//
//        }
//        - ValidationUtils -
//        이것은 옵션으로 강사님께서 넣어주신 것인데 스프링을 공부하다 보면 ValidationUtils를 볼 수 있다. 이러한 유틸리티 클래스가
//        있다는 정도로 알면 된다. 바로 위의 itemName에 대한 검증을 하는 로직과 아래의 ValidationUtils를 이용해서 만들어진
//        한줄의 코드는 똑같은 역할을 한다.
        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");

        if(item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
//            bindingResult.addError(new FieldError(
//                    "item",
//                    "price", item.getPrice(),
//                    false,
//                    new String[]{"range.item.price"},
//                    new Object[]{1000, 1000000},
//                    "가격은 1,000 ~ 1,000,000 까지 허용합니다." ));
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
            /*
            * 첫번째 인자: 필드이름 써준다.
            * 두번째 인자: 에러코드 써준다.
            * 세번재 인자: errors.properties의 에러코드(key-value 한쌍)가 인덱스를 참조해서
            *       defaultMessage를 만드는데 이때에 그 인덱스를 담고 있는 Object배열
            * 네번째 인자: defaultMessage를 써준다. 나는 null로 써줬다.
            * */

        }

        if(item.getQuantity() == null || item.getQuantity() >= 9999){
//            bindingResult.addError(new FieldError(
//                    "item",
//                    "quantity",
//                    item.getQuantity(),
//                    false,
//                    new String[]{"max.item.quantity"},
//                    new Object[]{9999},
//                    "수량은 최대 9,999 까지 허용합니다."));
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);

        }

        if(item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice < 10000){
//                bindingResult.addError(new ObjectError(
//                        "item",
//                        new String[]{"totalPriceMin"},
//                        new Object[]{10000, resultPrice},
//                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = "+resultPrice));
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
                /*
                * 특정 필드오류가 아니라 복합적인 룰을 벗어나서 생기는 오류일 경우에는 bindingResult.reject()를 통해서
                * bindingResult에 에러는 넣을 수 있다.
                *
                * 첫번쨰 인자 : 에러코드를 넣는다.
                * 두번쨰 인자 : errors.properties에 있는 key-value가 참조할 배열을 넣는다.
                * 세번째 인자 : defaultMessage를 넣는다. 나는 null을 넣었다.
                * */
            }
        }

        if(bindingResult.hasErrors()){
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

