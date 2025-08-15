import SwiftUI
import Combine
import Shared  // Assuming your shared module is imported

struct WordVerificationView: View {
    let viewModel: WordVerificationViewModel

    @State private var game: Game? = nil

    var body: some View {
        VStack {
            if let game = game {
                Text(game.gameState == .won ? "You Won!" : "Guess the Word")
                
                // Grid rendering...
            }
        }.onReceive(viewModel.gameStatePublisher) { newGame in
            self.game = newGame
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
