package ac.daejeon.app.service;

import ac.daejeon.app.common.CommonDao;
import ac.daejeon.app.common.CommonEmail;
import ac.daejeon.app.common.CommonVo;

import ac.daejeon.app.common.Statics;
import ac.daejeon.app.dao.AppDao;
import ac.daejeon.app.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AppService {

    private final AppDao appDao;
    private final CommonDao commonDao;
    private final CommonEmail commonEmail;
    private final PasswordEncoder passwordEncoder;



    //여권 사진 만들기
    public int createPassportFile(AppVo appVo, CommonVo commonVo) throws IOException {

        int filesIdx = Statics.saveFile(commonVo, commonDao, appVo.getPassportFile());

        if(filesIdx != 0) {
            appVo.setPassportFileIdx(filesIdx);
            appDao.createPassportFile(appVo);
            return 1;
        }

        return 0;
    }


    //여권 사진 가져오기
    public AppVo getInfoPassport(AppVo appVo, CommonVo commonVo) throws IOException {

        AppVo passportInfo = appDao.getInfoPassport(appVo);

        if(passportInfo.getPassportFileUuid() != null) {

            //String year, String month, String day, String uuid, String ext

            String year = Integer.toString(passportInfo.getPassportFileYear());
            String month = Integer.toString(passportInfo.getPassportFileMonth());
            String day = Integer.toString(passportInfo.getPassportFileDay());
            String uuid = passportInfo.getPassportFileUuid();
            String ext = passportInfo.getPassportFileExt();

            String fileBinary = Statics.unzipWithPassword(year, month, day, uuid, ext);

            passportInfo.setPassportFileBinary(fileBinary);
        }

        return passportInfo;
    }

    //이메일 학사 번호가 아직 인증되지 않았는지 확인
    public String checkEmailAndStudentId(AppVo appVo) {

        String resTxt = "";
        int isNotJoined = appDao.checkEmailAndStudentId(appVo);

        if(isNotJoined == 1) {
            String authCode = commonEmail.generateRandomAuthCode();
            appVo.setAuthCode(authCode);
            appDao.createAppJoinAuthCode(appVo);
            commonEmail.sendJoinAppAuthCodeEmail(authCode, appVo.getStudentEmail());
            resTxt = "sendEmail";
        } else {
            resTxt = "authed";
        }

        return resTxt;
    }


    //앱조인 코드 체크 맞으면 비밀번호 설정
    public String checkAppJoinAuthCode(AppVo appVo) {

        String resTxt = "";

        AppVo authCodeInfo = appDao.checkAppJoinAuthCode(appVo);
        if(appVo.getAuthCode().equals(authCodeInfo.getAuthCode())) {

            String encodePassword = passwordEncoder.encode(appVo.getPassword());
            appVo.setPassword(encodePassword);

            int successUpdate = appDao.setStudentPassword(appVo);

            if(successUpdate == 1) {
                resTxt = "authenticated";
            } else {
                resTxt = "notAuthenticated";
            }


        } else {
            resTxt = "notAuthenticated";
        }

        //System.out.println("인증코드 정보 " + authCodeInfo);

        return resTxt;
    }


    //개인정보 수정
    public int modifyPersonalInfo(AppVo appVo, CommonVo commonVo) throws IOException {

        if(appVo.isChangeFile()) {


            System.out.println("수정퍼스날인포111");

            int filesIdx = Statics.saveFile(commonVo, commonDao, appVo.getPersonalInfoFile());

            System.out.println("수정퍼스날인포222");
            if (filesIdx != 0) {
                appVo.setPersonalInfoFileIdx(filesIdx);
                appDao.modifyPersonalInfo(appVo);
                return 1;
            }

        } else {
            appDao.modifyPersonalInfo(appVo);
            System.out.println("수정퍼스날인포333");
            return 1;
        }

        return 0;
    }


    //토큰 생성
    public int setFcmToken(AppVo appVo) {

        AppVo studentInfo = appDao.getStudentInfo(appVo);

        if(studentInfo != null) {
            appVo.setStudentIdx(studentInfo.getStudentIdx());
            appVo.setStudentId(studentInfo.getStudentId());
            AppVo isExistFcmToken = appDao.isExistFcmToken(appVo);

            if (isExistFcmToken == null) {
                appDao.setFcmToken(appVo);
            } else {
                appDao.modifyFcmToken(appVo);
            }

        }

        /*AppVo studentInfo = appDao.getStudentInfo(appVo);

        if(studentInfo != null) {

            appVo.setStudentIdx(studentInfo.getStudentIdx());
            appVo.setStudentId(studentInfo.getStudentId());

            appDao.isExistFcmToken(appVo);


        }*/

        return 1;
    }


    public AppVo getPersonalInfo(AppVo appVo) throws IOException {

        AppVo personalInfo = appDao.getPersonalInfo(appVo);

        if(personalInfo.getPersonalInfoFileUuid() != null) {

            String year = Integer.toString(personalInfo.getPersonalInfoFileYear());
            String month = Integer.toString(personalInfo.getPersonalInfoFileMonth());
            String day = Integer.toString(personalInfo.getPersonalInfoFileDay());
            String uuid = personalInfo.getPersonalInfoFileUuid();
            String ext = personalInfo.getPersonalInfoFileExt();

            System.out.println("확인 " + year + " " + month + " " + day + " " + uuid + " " + ext);
            String fileBinary = Statics.unzipWithPassword(year, month, day, uuid, ext);
            System.out.println("바이너리 확인 " + fileBinary);

            personalInfo.setPersonalInfoFileBinary(fileBinary);

        }

        return personalInfo;
    }



    public int applicationSupportProgram(AppVo appVo) throws IOException {

        int exist = appDao.isAppliedSupportProgram(appVo);

        if(exist == 0) {
            return appDao.applicationSupportProgram(appVo);
        }

        return 0;

    }


    public AppVo getApplicationDetail(AppVo appVo) {
        return appDao.getApplicationDetail(appVo);
    }


    public int isAppliedSupportProgram(AppVo appVo) {
        return appDao.isAppliedSupportProgram(appVo);
    }



    public AppVo getVideoDetail(AppVo appVo) {
        return appDao.getVideoDetail(appVo);

    }

    public int sendViewingTime(AppVo appVo) {
        return appDao.sendViewingTime(appVo);
    }


    public AppVo getVideoWatched(AppVo appVo) {
        return appDao.getVideoWatched(appVo);
    }

    public List<AppVo> getClassSectionList(AppVo appVo) {
        return appDao.getClassSectionList(appVo);
    }



    public int getTodayAttendance(AppVo appVo) {

        LocalDate now = LocalDate.now();

        int year = now.getYear();
        int month = now.getMonth().getValue();
        int day = now.getDayOfMonth();

        appVo.setYear(year);
        appVo.setMonth(month);
        appVo.setDay(day);

        return appDao.getTodayAttendance(appVo);
    }


    public List<AppVo> getVideoList(AppVo appVo) {

        return appDao.getVideoList(appVo);
    }

    //로그인
    public LoginVo doLogin(LoginVo loginVo) {

        LoginVo loginInfos = appDao.doLogin(loginVo);

        if(loginInfos != null) {
            boolean isSamePassword = passwordEncoder.matches(loginVo.getStudentPassword(), loginInfos.getStudentPassword());

            loginInfos.setIsSamePassword(isSamePassword);
        } else {

        }

        return loginInfos;
    }

    public List<AppVo> getVideoPercentageList(AppVo appVo) {
        return appDao.getVideoPercentageList(appVo);
    }


    public List<AppVo> getNoticeList(AppVo appVo) {
        return appDao.getNoticeList(appVo);
    }

    public List<AppVo> getNoticeListForMain(AppVo appVo) {

        return appDao.getNoticeListForMain(appVo);
    }


    public List<SupportProgramVo> getCounselingList(SupportProgramVo supportProgramVo) {

        return appDao.getCounselingList(supportProgramVo);
    }

    public int cancelCounseling(SupportProgramVo supportProgramVo) {


        return appDao.cancelCounseling(supportProgramVo);
    }

    public int doCounseling(SupportProgramVo supportProgramVo) {


        return appDao.doCounseling(supportProgramVo);
    }

    //신청서 제출
    public int submitApplication(ApplicationVo applicationVo) {

        return appDao.submitApplication(applicationVo);
    }

    public int modifyApplication(ApplicationVo applicationVo) {

        return appDao.modifyApplication(applicationVo);
    }

    //여권정보
    public MyInfoVo getInfoForPassport(MyInfoVo myInfoVo) throws IOException {

        MyInfoVo infoForPassport = appDao.getInfoForPassport(myInfoVo);

        if(infoForPassport != null && infoForPassport.getPassportFileUuid() != null) {

            String year = Integer.toString(infoForPassport.getPassportFileYear());
            String month = Integer.toString(infoForPassport.getPassportFileMonth());
            String day = Integer.toString(infoForPassport.getPassportFileDay());
            String uuid = infoForPassport.getPassportFileUuid();
            String ext = infoForPassport.getPassportFileExt();

            //System.out.println("패스포드 파일 유유아이디 " + year + " " + month + " " + day + " " + uuid + " " + ext);
            String fileBinary = Statics.unzipWithPassword(year, month, day, uuid, ext);


            infoForPassport.setPassportFileBinary(fileBinary);
            //studentHistoryDetail.setPersonalInfoFileBinary(fileBinary);

        } else {

        }

        return infoForPassport;
    }

    //내프로필정보
    public MyInfoVo getInfoForProfile(MyInfoVo myInfoVo) throws IOException {

        MyInfoVo infoForProfile = appDao.getInfoForProfile(myInfoVo);

        if(infoForProfile != null && infoForProfile.getPersonalInfoFileUuid() != null) {

            String year = Integer.toString(infoForProfile.getPersonalInfoFileYear());
            String month = Integer.toString(infoForProfile.getPersonalInfoFileMonth());
            String day = Integer.toString(infoForProfile.getPersonalInfoFileDay());
            String uuid = infoForProfile.getPersonalInfoFileUuid();
            String ext = infoForProfile.getPersonalInfoFileExt();

            String fileBinary = Statics.unzipWithPassword(year, month, day, uuid, ext);

            infoForProfile.setPersonalInfoFileBinary(fileBinary);
            //studentHistoryDetail.setPersonalInfoFileBinary(fileBinary);

        } else {

        }

        return infoForProfile;

    }

    //외국인등록증
    public MyInfoVo getInfoForForeigner(MyInfoVo myInfoVo) throws IOException {

        MyInfoVo infoForForeigner = appDao.getInfoForForeigner(myInfoVo);

        if(infoForForeigner != null && infoForForeigner.getForeignerRegistrationFileUuid() != null) {

            String year = Integer.toString(infoForForeigner.getForeignerRegistrationFileYear());
            String month = Integer.toString(infoForForeigner.getForeignerRegistrationFileMonth());
            String day = Integer.toString(infoForForeigner.getForeignerRegistrationFileDay());
            String uuid = infoForForeigner.getForeignerRegistrationFileUuid();
            String ext = infoForForeigner.getForeignerRegistrationFileExt();

            String fileBinary = Statics.unzipWithPassword(year, month, day, uuid, ext);

            infoForForeigner.setForeignerRegistrationFileBinary(fileBinary);
            //studentHistoryDetail.setPersonalInfoFileBinary(fileBinary);

        } else {

        }

        return infoForForeigner;

    }

    public AppVo getStandardYearSemester() {
        return appDao.getStandardYearSemester();
    }



/*    //관리자 로그인
    public LoginVo doLogin(LoginVo loginVo) {



        return loginInfos;
    }*/
}
