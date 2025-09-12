package knu.team1.be.boost.projectMember.exception;

public class MemberAlreadyJoinedException extends RuntimeException {

    public MemberAlreadyJoinedException() {
        super("이미 참여중인 멤버입니다.");
    }
}
