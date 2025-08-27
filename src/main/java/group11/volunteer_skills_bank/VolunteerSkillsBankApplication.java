package group11.volunteer_skills_bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VolunteerSkillsBankApplication {

	public static void main(String[] args) {
		List<String> p = new ArrayList<>(List.of("LBcXbjbvLN1pSpwqiQMU", "09s11H3soHkfvTzYNXFV"));
		Collections.sort(p);
		System.out.println("Sorted value: " + p);
		System.out.println("Hashed value: " + DigestUtils.sha256Hex(String.join("_", p)));
		SpringApplication.run(VolunteerSkillsBankApplication.class, args);
	}

}
