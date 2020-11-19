package crack;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidARMEmulator;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.core.io.ClassPathResource;

public class DouyinSign extends AbstractJni {

    private final AndroidEmulator emulator;
    private final Module module;
    private final VM vm;

    private final DvmClass Native;
    static {
        String soPath = "example_binaries/libcms.so";
        ClassPathResource classPathResource = new ClassPathResource(soPath);
        try {
            InputStream inputStream = classPathResource.getInputStream();
            Files.copy(inputStream, Paths.get("./libcms.so"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private DouyinSign() {
        emulator = new AndroidARMEmulator("com.xxx.offical"); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23));// 设置系统类库解析

        vm = emulator.createDalvikVM(null); // 创建Android虚拟机
        vm.setJni(this);
        //vm.setVerbose(true);// 设置是否打印Jni调用细节

        // 自行修改文件路径,loadLibrary是java加载so的方法
        DalvikModule dm = vm.loadLibrary(new File("./libcms.so"), false); // 加载libcms.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数
        dm.callJNI_OnLoad(emulator);// 手动执行JNI_OnLoad函数
        module = dm.getModule();// 加载好的libcms.so对应为一个模块

        //leviathan所在的类，调用resolveClass解析该class对象
        Native = vm.resolveClass("com/ss/sys/ces/a");
    }

    private void destroy() throws IOException {
        emulator.close();
    }

    public static void main(String[] args) throws Exception {

        DouyinSign jnitest = new DouyinSign();
        String url =  "https://aweme-eagle.snssdk.com/aweme/v1/feed/?type=0&max_cursor=0&min_cursor=-1&count=30&volume=0.06666666666666667&pull_type=2&need_relieve_aweme=0&ts=1604989727&app_type=lite&manifest_version_code=180&_rticket=1604989727594&ac=wifi&device_id=2814349075811115&iid=1829220717444520&os_version=8.1.0&channel=xiaoshangdian_douyin_and19&version_code=180&device_type=Pixel&language=zh&resolution=1080*1758&openudid=2dc3087ecc9addf9&update_version_code=1800&app_name=aweme&version_name=1.8.0&os_api=27&device_brand=google&ssmix=a&device_platform=android&dpi=540&aid=1128&as=aac3468be85faa331fc346&cp=8c38c3468be8c3468c3032&mas=015993239979235313f3f9b9b9c56af3c75313f3f959b3f3f3b9b9";
        if(args.length>=1){
            url = args[0];
        }
        System.out.println("input url:"+url);
        jnitest.test(url);

        jnitest.destroy();
    }

    public static String genXGorgon(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        char[] charArray = "0123456789abcdef".toCharArray();
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int b2 = bArr[i] & 255;
            int i2 = i * 2;
            cArr[i2] = charArray[b2 >>> 4];
            cArr[i2 + 1] = charArray[b2 & 15];
        }
        return new String(cArr);
    }


    public static byte[] str2byte(String str) {
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static String getUrlParse(String str) {
        int indexOf = str.indexOf("?");
        int indexOf2 = str.indexOf("#");
        if (indexOf == -1) {
            return null;
        }
        if (indexOf2 == -1) {
            return str.substring(indexOf + 1);
        }
        if (indexOf2 > indexOf) {
            return null;
        }
        return str.substring(indexOf + 1, indexOf2);

    }

    private void test(String url) {
        // 调用so的入口，这是smali写法
        String methodSign = "leviathan(II[B)[B";

        //解析url里面的参数
        String a2 = getUrlParse(url);

        //字符串转md5
        String a3 = stringToMD5(a2);
        String str3 = "00000000000000000000000000000000";
        String str4 = "00000000000000000000000000000000";
        String str5 = "00000000000000000000000000000000";
        StringBuilder sb = new StringBuilder();
        sb.append(a3);
        sb.append(str3);
        sb.append(str4);
        sb.append(str5);

        byte[] data = str2byte(sb.toString());
        float currentTimeMillis = System.currentTimeMillis();
        int time = (int) (currentTimeMillis / 1000);

        Native.callStaticJniMethod(emulator, methodSign, -1, time, new ByteArray(vm, data));
        ByteArray ret = Native.callStaticJniMethodObject(emulator, methodSign, -1, time, new ByteArray(vm, data));

        // 获取地址的值
        byte[] tt = ret.getValue();
        //执行最外层的com.ss.a.b.a.a
        String s = genXGorgon(tt);
        System.out.println("X-Khronos:" + time);
        System.out.println("X-Gorgon:" + s);
    }
}