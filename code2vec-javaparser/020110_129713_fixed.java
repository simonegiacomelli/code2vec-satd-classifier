    public class Wrapper{
    @Override
    public void fixed() {
        if ((_id <= 0) || (_level <= 0)) {
            LOGGER.warn(RequestAcquireSkillInfo.class.getSimpleName() + "--##string##--" + _id + "--##string##--" + _level + "--##string##--");
            return;
        }
        final Player activeChar = client.getPlayer();
        if (activeChar == null) {
            return;
        }
        final Npc trainer = activeChar.getLastFolkNPC();
        if ((_skillType != AcquireSkillType.CLASS) && (!isNpc(trainer) || (!trainer.canInteract(activeChar) && !activeChar.isGM()))) {
            return;
        }
        final Skill skill = SkillData.getInstance().getSkill(_id, _level);
        if (skill == null) {
            LOGGER.warn("--##string##--" + _id + "--##string##--" + _level + "--##string##--" + RequestAcquireSkillInfo.class.getName() + "--##string##--");
            return;
        }
        final SkillLearn s = SkillTreesData.getInstance().getSkillLearn(_skillType, _id, _level, activeChar);
        if (s == null) {
            return;
        }
        switch(_skillType) {
            case TRANSFORM, FISHING:
                client.sendPacket(new AcquireSkillInfo(_skillType, s));
            case CLASS:
                client.sendPacket(new ExAcquireSkillInfo(activeChar, s));
            case PLEDGE:
                {
                    if (!activeChar.isClanLeader()) {
                        return;
                    }
                    client.sendPacket(new AcquireSkillInfo(_skillType, s));
                }
            case SUBPLEDGE:
                {
                    if (!activeChar.isClanLeader() || !activeChar.hasClanPrivilege(ClanPrivilege.CL_TROOPS_FAME)) {
                        return;
                    }
                    client.sendPacket(new AcquireSkillInfo(_skillType, s));
                }
        }
    }
}