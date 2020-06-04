package minegame159.meteorclient.mixin;

import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.EventStore;
import minegame159.meteorclient.events.RenderEvent;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.misc.UnfocusedCPU;
import minegame159.meteorclient.modules.render.NoHurtCam;
import minegame159.meteorclient.rendering.Matrices;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private Camera camera;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        if (ModuleManager.INSTANCE.isActive(UnfocusedCPU.class) && !client.isWindowFocused()) info.cancel();
    }

    @Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = { "ldc=hand" }), method = "renderCenter")
    public void onRenderCenter(float tickDelta, long endTime, CallbackInfo info) {
        if (!Utils.canUpdate()) return;

        client.getProfiler().swap("meteor-client_render");

        Matrices.begin();

        RenderEvent event = EventStore.renderEvent(tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);

        Renderer.begin(event);
        MeteorClient.EVENT_BUS.post(event);
        Renderer.end();
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onBobViewWhenHurt(float tickDelta, CallbackInfo info) {
        if (ModuleManager.INSTANCE.isActive(NoHurtCam.class)) info.cancel();
    }
}
