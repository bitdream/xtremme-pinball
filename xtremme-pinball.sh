#!/bin/bash

java -Djava.library.path=lib/lwjgl/native/linux:lib/ode/native/linux -classpath bin:lib/lwjgl/jinput.jar:lib/lwjgl/lwjgl_util_applet.jar:lib/lwjgl/lwjgl_util.jar:lib/lwjgl/lwjgl.jar:lib/jme.jar:lib/jme-audio.jar:lib/jme-awt.jar:lib/jme-collada.jar:lib/jme-editors.jar:lib/jme-effects.jar:lib/jme-font.jar:lib/jme-gamestates.jar:lib/jme-model.jar:lib/jme-scene.jar:lib/jme-swt.jar:lib/jme-terrain.jar:lib/jme-xml.jar:lib/jbullet/jbullet.jar:lib/jbullet/stack-alloc.jar:lib/jbullet/vecmath.jar:lib/ode/odejava-jni.jar:lib/jme-physics.jar:lib/jorbis/jorbis-0.0.17.jar:lib/FengGUI.jar:build/pinball.jar main.Main $*