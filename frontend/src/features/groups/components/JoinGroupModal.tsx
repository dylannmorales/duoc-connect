import { useState } from 'react';
import type { Grupo } from '@/features/groups/types/group.types';
import { Button, Input } from '@/shared/components/ui/FormControls';

interface JoinGroupModalProps {
  grupo: Grupo | null;
  open: boolean;
  onClose: () => void;
  onJoin: (grupoId: string, codigo?: string) => Promise<void>;
}

export function JoinGroupModal({ grupo, open, onClose, onJoin }: JoinGroupModalProps) {
  const [codigo, setCodigo] = useState('');
  const [loading, setLoading] = useState(false);

  if (!open || !grupo) return null;

  async function handleJoin() {
    setLoading(true);
    try {
      await onJoin(grupo.id, grupo.privado ? codigo : undefined);
      setCodigo('');
      onClose();
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h2 className="mb-2 text-xl font-bold">Unirse a {grupo.nombre}</h2>
        <p className="mb-6 text-sm text-gray-600">
          {grupo.privado
            ? 'Este grupo es privado. Ingresa el código de invitación.'
            : '¿Confirmas que quieres unirte a este grupo público?'}
        </p>

        {grupo.privado && (
          <Input
            label="Código de invitación"
            name="codigo"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value.toUpperCase())}
            placeholder="Ej: ABC123"
            className="mb-4"
            required
          />
        )}

        <div className="flex justify-end gap-3">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="button" onClick={handleJoin} loading={loading}>
            Unirse
          </Button>
        </div>
      </div>
    </div>
  );
}
