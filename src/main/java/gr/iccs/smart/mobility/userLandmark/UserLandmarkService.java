package gr.iccs.smart.mobility.userLandmark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLandmarkService {
    @Autowired
    private UserLandmarkRepository userLandmarkRepository;

    public void save(UserLandmark landmark) {
        userLandmarkRepository.save(landmark);
    }
}
