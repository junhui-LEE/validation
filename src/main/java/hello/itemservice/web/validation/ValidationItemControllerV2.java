package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
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

    @PostMapping("/add")
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

