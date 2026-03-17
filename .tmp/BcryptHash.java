public class BcryptHash {
    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "123456";
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        System.out.println(encoder.encode(password));
    }
}
