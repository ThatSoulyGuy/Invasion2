package com.thatsoulyguy.invasion2.input;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public enum KeyCode
{
    SPACE(GLFW.GLFW_KEY_SPACE),
    APOSTROPHE(GLFW.GLFW_KEY_APOSTROPHE),
    COMMA(GLFW.GLFW_KEY_COMMA),
    MINUS(GLFW.GLFW_KEY_MINUS),
    PERIOD(GLFW.GLFW_KEY_PERIOD),
    SLASH(GLFW.GLFW_KEY_SLASH),
    ZERO(GLFW.GLFW_KEY_0),
    ONE(GLFW.GLFW_KEY_1),
    TWO(GLFW.GLFW_KEY_2),
    THREE(GLFW.GLFW_KEY_3),
    FOUR(GLFW.GLFW_KEY_4),
    FIVE(GLFW.GLFW_KEY_5),
    SIX(GLFW.GLFW_KEY_6),
    SEVEN(GLFW.GLFW_KEY_7),
    EIGHT(GLFW.GLFW_KEY_8),
    NINE(GLFW.GLFW_KEY_9),
    SEMICOLON(GLFW.GLFW_KEY_SEMICOLON),
    EQUAL(GLFW.GLFW_KEY_EQUAL),
    A(GLFW.GLFW_KEY_A),
    B(GLFW.GLFW_KEY_B),
    C(GLFW.GLFW_KEY_C),
    D(GLFW.GLFW_KEY_D),
    E(GLFW.GLFW_KEY_E),
    F(GLFW.GLFW_KEY_F),
    G(GLFW.GLFW_KEY_G),
    H(GLFW.GLFW_KEY_H),
    I(GLFW.GLFW_KEY_I),
    J(GLFW.GLFW_KEY_J),
    K(GLFW.GLFW_KEY_K),
    L(GLFW.GLFW_KEY_L),
    M(GLFW.GLFW_KEY_M),
    N(GLFW.GLFW_KEY_N),
    O(GLFW.GLFW_KEY_O),
    P(GLFW.GLFW_KEY_P),
    Q(GLFW.GLFW_KEY_Q),
    R(GLFW.GLFW_KEY_R),
    S(GLFW.GLFW_KEY_S),
    T(GLFW.GLFW_KEY_T),
    U(GLFW.GLFW_KEY_U),
    V(GLFW.GLFW_KEY_V),
    W(GLFW.GLFW_KEY_W),
    X(GLFW.GLFW_KEY_X),
    Y(GLFW.GLFW_KEY_Y),
    Z(GLFW.GLFW_KEY_Z),

    LEFT_BRACKET(GLFW.GLFW_KEY_LEFT_BRACKET),
    BACKSLASH(GLFW.GLFW_KEY_BACKSLASH),
    RIGHT_BRACKET(GLFW.GLFW_KEY_RIGHT_BRACKET),
    GRAVE_ACCENT(GLFW.GLFW_KEY_GRAVE_ACCENT),
    WORLD_1(GLFW.GLFW_KEY_WORLD_1),
    WORLD_2(GLFW.GLFW_KEY_WORLD_2),

    ESCAPE(GLFW.GLFW_KEY_ESCAPE),
    ENTER(GLFW.GLFW_KEY_ENTER),
    TAB(GLFW.GLFW_KEY_TAB),
    BACKSPACE(GLFW.GLFW_KEY_BACKSPACE),
    INSERT(GLFW.GLFW_KEY_INSERT),
    DELETE(GLFW.GLFW_KEY_DELETE),
    RIGHT(GLFW.GLFW_KEY_RIGHT),
    LEFT(GLFW.GLFW_KEY_LEFT),
    DOWN(GLFW.GLFW_KEY_DOWN),
    UP(GLFW.GLFW_KEY_UP),
    PAGE_UP(GLFW.GLFW_KEY_PAGE_UP),
    PAGE_DOWN(GLFW.GLFW_KEY_PAGE_DOWN),
    HOME(GLFW.GLFW_KEY_HOME),
    END(GLFW.GLFW_KEY_END),
    CAPS_LOCK(GLFW.GLFW_KEY_CAPS_LOCK),
    SCROLL_LOCK(GLFW.GLFW_KEY_SCROLL_LOCK),
    NUM_LOCK(GLFW.GLFW_KEY_NUM_LOCK),
    PRINT_SCREEN(GLFW.GLFW_KEY_PRINT_SCREEN),
    PAUSE(GLFW.GLFW_KEY_PAUSE),

    // Function keys F1 to F25
    F1(GLFW.GLFW_KEY_F1),
    F2(GLFW.GLFW_KEY_F2),
    F3(GLFW.GLFW_KEY_F3),
    F4(GLFW.GLFW_KEY_F4),
    F5(GLFW.GLFW_KEY_F5),
    F6(GLFW.GLFW_KEY_F6),
    F7(GLFW.GLFW_KEY_F7),
    F8(GLFW.GLFW_KEY_F8),
    F9(GLFW.GLFW_KEY_F9),
    F10(GLFW.GLFW_KEY_F10),
    F11(GLFW.GLFW_KEY_F11),
    F12(GLFW.GLFW_KEY_F12),
    F13(GLFW.GLFW_KEY_F13),
    F14(GLFW.GLFW_KEY_F14),
    F15(GLFW.GLFW_KEY_F15),
    F16(GLFW.GLFW_KEY_F16),
    F17(GLFW.GLFW_KEY_F17),
    F18(GLFW.GLFW_KEY_F18),
    F19(GLFW.GLFW_KEY_F19),
    F20(GLFW.GLFW_KEY_F20),
    F21(GLFW.GLFW_KEY_F21),
    F22(GLFW.GLFW_KEY_F22),
    F23(GLFW.GLFW_KEY_F23),
    F24(GLFW.GLFW_KEY_F24),
    F25(GLFW.GLFW_KEY_F25),

    KEYPAD_ZERO(GLFW.GLFW_KEY_KP_0),
    KEYPAD_ONE(GLFW.GLFW_KEY_KP_1),
    KEYPAD_TWO(GLFW.GLFW_KEY_KP_2),
    KEYPAD_THREE(GLFW.GLFW_KEY_KP_3),
    KEYPAD_FOUR(GLFW.GLFW_KEY_KP_4),
    KEYPAD_FIVE(GLFW.GLFW_KEY_KP_5),
    KEYPAD_SIX(GLFW.GLFW_KEY_KP_6),
    KEYPAD_SEVEN(GLFW.GLFW_KEY_KP_7),
    KEYPAD_EIGHT(GLFW.GLFW_KEY_KP_8),
    KEYPAD_NINE(GLFW.GLFW_KEY_KP_9),
    KP_DECIMAL(GLFW.GLFW_KEY_KP_DECIMAL),
    KP_DIVIDE(GLFW.GLFW_KEY_KP_DIVIDE),
    KP_MULTIPLY(GLFW.GLFW_KEY_KP_MULTIPLY),
    KP_SUBTRACT(GLFW.GLFW_KEY_KP_SUBTRACT),
    KP_ADD(GLFW.GLFW_KEY_KP_ADD),
    KP_ENTER(GLFW.GLFW_KEY_KP_ENTER),
    KP_EQUAL(GLFW.GLFW_KEY_KP_EQUAL),

    LEFT_SHIFT(GLFW.GLFW_KEY_LEFT_SHIFT),
    LEFT_CONTROL(GLFW.GLFW_KEY_LEFT_CONTROL),
    LEFT_ALT(GLFW.GLFW_KEY_LEFT_ALT),
    LEFT_SUPER(GLFW.GLFW_KEY_LEFT_SUPER),
    RIGHT_SHIFT(GLFW.GLFW_KEY_RIGHT_SHIFT),
    RIGHT_CONTROL(GLFW.GLFW_KEY_RIGHT_CONTROL),
    RIGHT_ALT(GLFW.GLFW_KEY_RIGHT_ALT),
    RIGHT_SUPER(GLFW.GLFW_KEY_RIGHT_SUPER),
    MENU(GLFW.GLFW_KEY_MENU),

    UNKNOWN(-1);

    private final int value;

    KeyCode(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static @NotNull KeyCode fromGLFWKey(int glfwKey)
    {
        for (KeyCode keyCode : KeyCode.values())
        {
            if (keyCode.getValue() == glfwKey)
                return keyCode;
        }

        return KeyCode.UNKNOWN;
    }
}