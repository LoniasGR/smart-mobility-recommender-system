package gr.iccs.smart.mobility.userlandmark;

import org.springframework.stereotype.Service;

@Service
public class UserLandmarkService {
    private UserLandmarkRepository userLandmarkRepository;

    UserLandmarkService(UserLandmarkRepository userLandmarkRepository) {
        this.userLandmarkRepository = userLandmarkRepository;
    }

    public void save(UserLandmark landmark) {
        userLandmarkRepository.save(landmark);
    }

    public void delete(UserLandmark landmark) {
        userLandmarkRepository.delete(landmark);
    }
}
