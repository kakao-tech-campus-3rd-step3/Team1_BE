package knu.team1.be.boost.notification.event.dto;

import knu.team1.be.boost.member.entity.Member;

public record NotificationSavedEvent(
    Member member,
    String title,
    String message
) {

    public static NotificationSavedEvent from(
        Member member,
        String title,
        String message
    ) {
        return new NotificationSavedEvent(member, title, message);
    }
}
