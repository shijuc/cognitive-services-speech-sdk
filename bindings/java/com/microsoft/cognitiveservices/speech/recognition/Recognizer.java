package com.microsoft.cognitiveservices.speech.recognition;
//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

import java.io.Closeable;
import java.io.IOException;

import com.microsoft.cognitiveservices.speech.SessionEventArgs;
import com.microsoft.cognitiveservices.speech.SessionEventType;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;

/// <summary>
/// Defines the base class Recognizer which mainly contains common event handlers.
/// </summary>
public class Recognizer implements Closeable
{
    /// <summary>
    /// Defines event handler for session events, e.g. SessionStarted/Stopped, SpeechStartDetected / StopDetected
    /// </summary>
    /// <example>
    /// Create a speech recognizer, setup an event handler for session events
    /// <code>
    /// static void MySessionEventHandler(object sender, SpeechSessionEventArgs e)
    /// {
    ///    Console.WriteLine(String.Format("Speech recognition: session event: {0} ", e.ToString()));
    /// }
    ///
    /// static void SpeechRecognizerSample()
    /// {
    ///   SpeechRecognizer reco = factory.CreateSpeechRecognizer("audioFileName");
    ///
    ///   reco.OnSessionEvent += MySessionEventHandler;
    ///
    ///   // Starts recognition.
    ///   var result = await reco.RecognizeAsync();
    ///
    ///   reco.OnSessionEvent -= MySessionEventHandler;
    ///  
    ///   Console.WriteLine("Speech Recognition: Recognition result: " + result);
    /// }
    /// </code>
    /// </example>
    final public EventHandlerImpl<SessionEventArgs> SessionEvent = new EventHandlerImpl<SessionEventArgs>();

    protected Recognizer()
    {
        sessionStartedHandler = new SessionEventHandlerImpl(this, SessionEventType.SessionStartedEvent);
        sessionStoppedHandler = new SessionEventHandlerImpl(this, SessionEventType.SessionStoppedEvent);
        speechStartDetectedHandler = new SessionEventHandlerImpl(this, SessionEventType.SpeechStartDetectedEvent);
        speechEndDetectedHandler = new SessionEventHandlerImpl(this, SessionEventType.SpeechEndDetectedEvent);
    }

    /// <summary>
    /// Dispose of associated resources.
    /// </summary>
    public void close()
    {
        try {
            dispose(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /// <summary>
    /// This method performs cleanup of resources.
    /// The Boolean parameter <paramref name="disposing"/> indicates whether the method is called from <see cref="IDisposable.Dispose"/> (if <paramref name="disposing"/> is true) or from the finalizer (if <paramref name="disposing"/> is false).
    /// Derived classes should override this method to dispose resource if needed.
    /// </summary>
    /// <param name="disposing">Flag to request disposal.</param>
    protected void dispose(boolean disposing) throws IOException
    {
        if (disposed)
        {
            return;
        }

        if (disposing)
        {
            // disconnect
            sessionStartedHandler.delete();
            sessionStoppedHandler.delete();
            speechStartDetectedHandler.delete();
            speechEndDetectedHandler.delete();
        }

        disposed = true;
    }

    protected SessionEventHandlerImpl sessionStartedHandler;
    protected SessionEventHandlerImpl sessionStoppedHandler;
    protected SessionEventHandlerImpl speechStartDetectedHandler;
    protected SessionEventHandlerImpl speechEndDetectedHandler;
    private boolean disposed = false;

    /// <summary>
    /// Define an internal class which raise a C# event when a corresponding callback is invoked from the native layer. 
    /// </summary>
    class SessionEventHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.SessionEventListener
    {
        public SessionEventHandlerImpl(Recognizer recognizer, SessionEventType eventType)
        {
            this.recognizer = recognizer;
            this.eventType = eventType;
        }

        @Override
        public void execute(com.microsoft.cognitiveservices.speech.internal.SessionEventArgs eventArgs)
        {
            if (recognizer.disposed)
            {
                return;
            }

            SessionEventArgs arg = new SessionEventArgs(eventType, eventArgs);
            EventHandlerImpl<SessionEventArgs>  handler = this.recognizer.SessionEvent;

            if (handler != null)
            {
                handler.fireEvent(this.recognizer, arg);
            }
        }

        private Recognizer recognizer;
        private SessionEventType eventType;
        
    }
}
