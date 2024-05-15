package hello.itemservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

}


//	@SpringBootApplication
//	public class ItemServiceApplication implements WebMvcConfigurer {
//
//		public static void main(String[] args) {
//			SpringApplication.run(ItemServiceApplication.class, args);
//		}
//
//		/*
//			  * Validator(검증기) 글로벌 설정 - 모든 컨트롤러에 다 적용 *
//		 ItemServiceApplication이 WebMvcConfigurer을 구현하고
//		 아래와 같이 코드롤 써주면 모든 요청에 대해서 생성되는 WebDataBinder에 ItemValidator검증기를
//		 적용시키는 것이다. 그럼 적용하려면 컨트롤러 안의 target 객체 앞에 @Validated를 써주면 된다.
//		 이렇게 하면 모든 요청에 대해서 ItemValidator검증기를 적용시킬수 있다.
//		 하나의 컨트롤러에 검증기를 적용시키려면 WebDataBinder을 억지로 꺼낸 다음에 검증기를 적용시켜야 한다.
//		 */
//	//	@Override
//	//	public Validator getValidator(){
//	//		return new ItemValidator();
//	//	}
//
//	}