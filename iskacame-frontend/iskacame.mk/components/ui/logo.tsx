import {
    OpaqueColorValue,
    StyleProp,
    StyleSheet,
    Text,
    TextStyle,
    Image,
} from "react-native";

export function Logo({
                         isLogoOnly = false,
                         size = 38,
                         // color = "#F0EBE1",
                         color = "#B8AEDE",
                         style,
                     }: {
    isLogoOnly?: boolean;
    size?: number;
    color?: string | OpaqueColorValue;
    style?: StyleProp<TextStyle>;
}) {

    if (isLogoOnly) {
        return (
            <Image
                source={require("../../assets/images/favicon.png")}
                style={{ width: size, height: size }}
                resizeMode="contain"
            />
        );
    }

    return (
        <>
            <Text style={[styles.logo, { fontSize: size }, style,]}>
                iskacame
            </Text>
            <Text style={[styles.logoSub, { fontSize: size, color },]}>
                .mk
            </Text>
        </>
    );
}

const styles = StyleSheet.create({
    logo: {
        fontWeight: "800",
        letterSpacing: -1.5,
        color: "#F0EBE1",
    },
    logoSub: {
        fontWeight: "800",
        letterSpacing: -1.5,
    },
});