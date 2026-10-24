package me.bronsk.aethercore.text;

@FunctionalInterface
public interface AETextResolver {

    String resolve(AETextContext context);
}
