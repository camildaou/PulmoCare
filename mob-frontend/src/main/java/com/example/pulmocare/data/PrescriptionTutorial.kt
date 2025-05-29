package com.example.pulmocare.data

data class PrescriptionTutorial(
    val medicationName: String,  // Name of the medication (should match the document name)
    val videoTitle: String,      // Title of the tutorial video
    val videoDescription: String, // Short description of what the video teaches
    val videoUrl: String,        // YouTube URL for the video
    val thumbnailUrl: String,    // Thumbnail URL for the video
    val medicationType: String,  // Type of medication (e.g., "Rescue Inhaler", "Maintenance Inhaler", etc.)
    val importantNotes: List<String> = emptyList() // Important notes about using the medication
)

object PrescriptionTutorials {
    val tutorials = mapOf(
        "Albuterol Inhaler" to PrescriptionTutorial(
            medicationName = "Albuterol Inhaler",            
            videoTitle = "How to Use Your Albuterol Inhaler",
            videoDescription = "Step-by-step guide on how to properly use your albuterol rescue inhaler for asthma or COPD.",            
            videoUrl = "https://www.youtube.com/watch?v=2i9_DelNqs4",
            thumbnailUrl = "https://img.youtube.com/vi/2i9_DelNqs4/0.jpg",
            medicationType = "Rescue Inhaler",
            importantNotes = listOf(
                "Shake the inhaler well before each use",
                "Wait at least 1 minute between puffs if multiple doses are prescribed",
                "Rinse your mouth with water after use to prevent irritation",
                "Keep track of how often you use your rescue inhaler; increased use may indicate worsening condition"
            )
        ),        
        "Fluticasone" to PrescriptionTutorial(
            medicationName = "Fluticasone",
            videoTitle = "Using Your Fluticasone Inhaler Correctly",
            videoDescription = "Learn the proper technique for using your fluticasone inhaler to manage asthma or COPD symptoms.",
            videoUrl = "https://www.youtube.com/watch?v=GYZBkjHW_0k",
            thumbnailUrl = "https://img.youtube.com/vi/GYZBkjHW_0k/0.jpg",
            medicationType = "Maintenance Inhaler",
            importantNotes = listOf(
                "Use this medication regularly as prescribed, even if you feel well",
                "Rinse your mouth with water after each use to prevent oral thrush",
                "It may take 1-2 weeks before you feel the full benefit",
                "This medication does not replace your rescue inhaler for sudden symptoms"
            )
        ),          
        "CPAP Device" to PrescriptionTutorial(
            medicationName = "CPAP Device",
            videoTitle = "Setting Up and Using Your CPAP Machine",
            videoDescription = "Comprehensive guide on how to set up, use, and maintain your CPAP device for sleep apnea.",
            videoUrl = "https://www.youtube.com/watch?v=5P6KFIG6zaY",
            thumbnailUrl = "https://img.youtube.com/vi/5P6KFIG6zaY/0.jpg",
            medicationType = "Sleep Apnea Device",
            importantNotes = listOf(
                "Clean your mask, tubing and water chamber regularly",
                "Replace filters and other components as recommended by the manufacturer",
                "Adjust the mask to prevent air leaks but not so tight that it causes discomfort",
                "Contact your healthcare provider if you experience continued discomfort or side effects"
            )
        ),        
        "Fluticasone Inhaler" to PrescriptionTutorial(
            medicationName = "Fluticasone Inhaler",
            videoTitle = "Using Your Fluticasone Inhaler Correctly",
            videoDescription = "Learn the proper technique for using your fluticasone inhaler to manage asthma or COPD symptoms.",
            videoUrl = "https://www.youtube.com/watch?v=GYZBkjHW_0k",
            thumbnailUrl = "https://img.youtube.com/vi/GYZBkjHW_0k/0.jpg",
            medicationType = "Maintenance Inhaler",
            importantNotes = listOf(
                "Use this medication regularly as prescribed, even if you feel well",
                "Rinse your mouth with water after each use to prevent oral thrush",
                "It may take 1-2 weeks before you feel the full benefit",
                "This medication does not replace your rescue inhaler for sudden symptoms"
            )
        ),        
        "Montelukast" to PrescriptionTutorial(
            medicationName = "Montelukast",
            videoTitle = "Montelukast (Singulair): What You Need to Know",
            videoDescription = "Important information about Montelukast, how it works for asthma and allergies, and potential side effects.",            videoUrl = "https://www.youtube.com/watch?v=N9VV-NRxG_o",
            thumbnailUrl = "https://img.youtube.com/vi/N9VV-NRxG_o/0.jpg",
            medicationType = "Oral Medication",
            importantNotes = listOf(
                "Take this medication exactly as prescribed, usually once daily in the evening",
                "Monitor for mood changes or behavioral side effects, which should be reported immediately",
                "Continue taking this medication even during symptom-free periods",
                "This medication is not for treating acute asthma attacks"
            )
        )
    )
}
