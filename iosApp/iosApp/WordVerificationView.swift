import SwiftUI
import shared  // Assuming your shared module is imported

struct WordVerificationView: View {
    @ObservedObject var viewModel: WordVerificationViewModel  // Expose ViewModel in iOSMain or via helper

    var body: some View {
        VStack {
            // Game Status
            Text(viewModel.gameState.gameState == .won ? "You Won!" : "Guess the Word")
            
            // Grid
            Grid {
                ForEach(0..<viewModel.gameState.guesses.count) { row in
                    GridRow {
                        ForEach(viewModel.gameState.guesses[row].letters) { letter in
                            Text(letter.char)
                                .frame(maxWidth: .infinity)
                                .background(colorForState(letter.state))
                        }
                    }
                }
            }
            
            // Keyboard (simplified)
            // Custom views for keys, calling viewModel.addLetter, etc.
        }
    }
    
    func colorForState(_ state: LetterState) -> Color {
        switch state {
        case .correct: return .green
        case .wrongPosition: return .yellow
        case .notInWord: return .gray
        default: return .white
        }
    }
}
