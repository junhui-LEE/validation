package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000")
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000", message = "10000원 넘게 입력해 주세요")
/*
* 그런데 @ScriptAssert를 실제로 사용해보면 제약이 많고 복잡하다. 그리고 실무에서는 검증기능이 해당 객체의 범위를
* 넘어서는 경우들도 종종 등장하는데, 그런 경우 대응이 어렵다.
*
* 따라서 오브젝트 오류(글로벌 오류)의 경우 @ScriptAssert를 억지로 사용하는 것 보다는 오브젝트 오류 관련 부분만
* 직접 자바 코드로 작성하는 것을 권장한다.
* */
public class Item {

    private Long id;

//    @NotBlank(message = "공백X")
//    @NotBlank(message = "공백X {0}")
    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000, max=1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
