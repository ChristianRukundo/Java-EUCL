package rca.ac.rw.template.meter;

import rca.ac.rw.template.meter.dto.MeterResponseDto;
import rca.ac.rw.template.users.User;
import rca.ac.rw.template.users.dtos.UserMiniResponseDto; 

public class MeterConverter {

    public static MeterResponseDto toDto(Meter meter) {
        if (meter == null) {
            return null;
        }
        UserMiniResponseDto userDto = null;
        if (meter.getUser() != null) {
            User user = meter.getUser();
            userDto = new UserMiniResponseDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
        }
        return new MeterResponseDto(
                meter.getId(),
                meter.getMeterNumber(),
                userDto,
                meter.getCreatedAt(),
                meter.getUpdatedAt()
        );
    }

}